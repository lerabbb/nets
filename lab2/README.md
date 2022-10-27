# Nets. Lab2 "Sending files by TCP and calculating data transfer rates"
This client-server program transfers a file by TCP. Client sends any file to server with byte blocks. Server receives byte blocks and calculate data transfer speed each 3 seconds. After this server sends to client a success flag.

* Build project:\
./gradlew build\


* To run client module:\
./gradlew client:run --args='file.txt 127.0.0.1 8888'


* To run server module:\
./gradlew server:run --args='8888'
