import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.spark.sql.{SaveMode, SparkSession}

import java.io.File
import java.util.UUID

case class Book(name: String, cost: Int, year: Int)

case class LakeFSProperties(credentials: Credentials, server: Server)

case class Server(endpoint_url: String) {
  require(endpoint_url != null, "endpoint_url is null")
}

case class Credentials(access_key_id: String, secret_access_key: String) {
  require(access_key_id != null, "endpoint_url is null")
  require(secret_access_key != null, "endpoint_url is null")
}

object Main extends Loggable {
  val CONFIG_FILE_NAME = "lakectl.yaml"
  lazy val (accessKey: String, secretKey: String, endpoint: String) = {
    val configFile = new File(CONFIG_FILE_NAME)
    require(configFile.exists(), s"$CONFIG_FILE_NAME doesn't exist")
    val mapper: ObjectMapper = new ObjectMapper(new YAMLFactory())
    mapper.registerModule(DefaultScalaModule)
    val prop: LakeFSProperties = mapper.readValue(configFile, classOf[LakeFSProperties])
    (prop.credentials.access_key_id, prop.credentials.secret_access_key, prop.server.endpoint_url)
  }
  lazy val spark = SparkSession
    .builder()
    .appName("Spark LakeFS example")
    .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
    .config("spark.hadoop.fs.s3a.endpoint", endpoint.replaceFirst("api.*", ""))
    .config("spark.hadoop.fs.s3a.access.key", accessKey)
    .config("spark.hadoop.fs.s3a.secret.key", secretKey)
    .config("spark.hadoop.fs.s3a.path.style.access", "true")
    .config("spark.logConf", "true")
    .master("local[*]")
    .getOrCreate()


  def main(args: Array[String]) {
    val repoName = "my-repo"
    val mainBranchName = "main"
    val branchName = "test" + UUID.randomUUID()
    val path = CloudPath(repoName, branchName)
    val tableName = "table2"
    runWorkflow(path, mainBranchName, tableName)
    read(CloudPath(branchName, mainBranchName), tableName)

  }

  private def runWorkflow(path: CloudPath, mainBranch: String, tablePath: String) = {
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
    val bucketPath = createDataPath(path, tablePath)
    log.info(s"Saving to $bucketPath")
    df.write
      .partitionBy("year")
      .mode(SaveMode.Overwrite)
      .parquet(bucketPath)
  }

  private def createDataPath(path: CloudPath, tablePath: String) = {
    s"s3a://${path.repo}/${path.branch}/$tablePath/"
  }
}
