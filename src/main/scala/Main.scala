import org.apache.spark.sql.{SaveMode, SparkSession}

import java.io.{File, FileReader}
import java.util.{Properties, UUID}
import scala.collection.JavaConverters._

case class Book(name: String, cost: Int, year: Int)

object Main {
  lazy val (accessKey: String, secretKey: String, endpoint: String) = {
    val filePath = s"${new File(".").getCanonicalPath}/.env"
    val properties = new Properties()
    properties.load(new FileReader(filePath))
    val map = properties.asScala
    (map.getOrElse("accessKey", throw createPropNotFoundException("accessKey")),
      map.getOrElse("secretKey", throw createPropNotFoundException("secretKey")),
      map.getOrElse("endpoint", throw createPropNotFoundException("endpoint")))
  }

  def createPropNotFoundException(propName: String) = new RuntimeException(s"Cannot find $propName")

  lazy val spark = SparkSession
    .builder()
    .appName("Spark LakeFS example")
    .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
    .config("spark.hadoop.fs.s3a.endpoint", endpoint)
    .config("spark.hadoop.fs.s3a.access.key", accessKey)
    .config("spark.hadoop.fs.s3a.secret.key", secretKey)
    .config("spark.hadoop.fs.s3a.path.style.access", "true")
    .master("local[*]")
    .getOrCreate()


  def main(args: Array[String]) {
    val path = CloudPath("test", "test" + UUID.randomUUID())
    write(path, "main", "table4")
    read(CloudPath("test", "main"), "table4")

  }

  private def write(path: CloudPath, mainBranch: String, tablePath: String) = {
    val lakeFSApi = new LakeFSApi(accessKey, secretKey, endpoint)
    lakeFSApi.createBranch(path, mainBranch)
    doWork(path, tablePath)
    lakeFSApi.commit(path)
    lakeFSApi.merge(path, mainBranch)
    lakeFSApi.deleteBranch(path)
  }

  private def read(path: CloudPath, tablePath: String) = {
    val df = spark.read.parquet(createDataPath(path, tablePath))
    df.show()
  }

  private def doWork(path: CloudPath, tablePath: String) = {
    import spark.implicits._
    val df = spark.createDataset(spark.sparkContext.parallelize(Seq(
      Book("Spark", 1, 1970),
      Book("Kafka", 1, 1971),
      Book("Scala", 1, 1972),
    )))
    df.show()
    df.write
      .partitionBy("year")
      .mode(SaveMode.Overwrite)
      .parquet(createDataPath(path, tablePath))
  }

  private def createDataPath(path: CloudPath, tablePath: String) = {
    s"s3a://${path.repo}/${path.branch}/$tablePath/"
  }
}
