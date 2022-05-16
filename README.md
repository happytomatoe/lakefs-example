This is sample repo to show how to use [LakeFS](https://lakefs.io/)


In [Main.scala](src/main/scala/Main.scala) next git workflow is used
- create new branch
- update some table using spark
- commit changes
- merges changes
- delete branch

## How to run:
- ```console docker compose up -d ```
- Go to [MinIo UI](http://localhost:9001)
- Login using MINIO_ROOT_USER as username and MINIO_ROOT_PASSWORD as password from [docker-compose.yml](docker-compose.yml) 
- Create a new bucket
- Rename .env.template to .env
- Go to [LakeFS UI](http://localhost:8000/)
- After entering admin username Save
1) Key ID value as accessKey in [.env](.env)
2) Secret Key to secretKey in [.env](.env)
- Create new repository and entered the path to the bucket that you've created in MinIO 
- Change repoName in Main.main function
- Run Main.scala
