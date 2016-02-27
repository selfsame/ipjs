# js code linking with ipfs

## setup

* install ipfs (https://ipfs.io/)
  * `ipfs init`
  * `ipfs daemon`
  * allow CORS for localhost ipfs server
    * `ipfs config --json API.HTTPHeaders.Access-Control-Allow-Origin "[\"*\"]"`
    * `ipfs config --json API.HTTPHeaders.Access-Control-Allow-Methods "[\"PUT\", \"GET\", \"POST\"]"`
    * `ipfs config --json API.HTTPHeaders.Access-Control-Allow-Credentials "[\"true\"]"`
* cljs
  * start figwheel `lein figwheel`