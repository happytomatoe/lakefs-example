import io.lakefs.clients.api.model.{BranchCreation, CommitCreation, Merge}
import io.lakefs.clients.api._

import java.time.LocalDateTime
import java.util.UUID

class LakeFSApi(accessKey: String, secretKey: String, endpoint: String) {
  lazy val defaultClient: ApiClient = {
    val cl = Configuration.getDefaultApiClient
    cl.setBasePath(s"$endpoint/api/v1")
    cl.setUsername(accessKey)
    cl.setPassword(secretKey)
    cl
  }

  def createBranch(path: CloudPath, source: String) = {
    val api = new BranchesApi(defaultClient)
    api.createBranch(path.repo, new BranchCreation().name(path.branch).source(source))
  }

  def deleteBranch(path: CloudPath) = {
    new BranchesApi(defaultClient).deleteBranch(path.repo, path.branch)
  }

  def merge(path: CloudPath, destinationBranch: String) = {
    val api = new RefsApi(defaultClient)
    val merge = new Merge().message(s"Merging new info " + LocalDateTime.now().toString)
      .putMetadataItem("jobId", UUID.randomUUID().toString)
    api.mergeIntoBranch(path.repo, path.branch, destinationBranch, merge)
  }

  def commit(path: CloudPath) = {
    val api = new CommitsApi(defaultClient)
    api.commit(path.repo, path.branch, new CommitCreation().message("Committing " + LocalDateTime.now().toString)
      .putMetadataItem("test", "test"))
  }
}
