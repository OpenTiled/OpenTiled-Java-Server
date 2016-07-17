<img src="https://raw.githubusercontent.com/OpenTiled/OpenTiled-Java-Server/master/logos/OpenTiledLogo.png" width="250">
# OpenTiled Server

OpenTiled Server is a java server that allows multiple clients to edit one tile map. This project is based on the Tiled map editor: https://github.com/bjorn/tiled

To begin download the server: https://github.com/OpenTiled/OpenTiled-Java-Server/blob/master/dist/OpenTiled-Server.jar?raw=true

Direct users to https://github.com/OpenTiled/OpenTiled-Java-Client to access your server

### Run server:
Have java installed and run:
```sh
java -jar OpenTiled-Server.jar [args]
```

```sh
args:
-p [port to listen]
-inf [file path of input tmx file]
-tsf [file path to input tileset image]
-sdir [file path directory to save snaphots]
-sfq [number of mapchanges will trigger a snapshot to be taken]
```
### Default :
- The server needs an input file to initalize the tilemap. The OpenTiled Server will look for map.tmx in the current working directory of the OpenTiled Server. Download an example map: https://raw.githubusercontent.com/OpenTiled/OpenTiled-Java-Server/master/dist/map.tmx
- The server needs an input image to initialize the tileset for the tile map. The OpenTiled Server will look for tileset.png in the current working directory of the OpenTiled Server. Download an example tileset from: https://github.com/OpenTiled/OpenTiled-Java-Server/blob/master/dist/tileset.png
- Every 40 map changes a snapshot of the tilemap will be saved.
- Snapshots will be saved under ./snapshots by default
- The default port is 9000.