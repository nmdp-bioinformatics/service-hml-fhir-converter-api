#!/bin/sh

echo "Importing model building script from git."

SRC_PATH=`pwd`

curl -LJO https://raw.githubusercontent.com/nmdp-bioinformatics/util-swagger-codegen-models/master/Download.py

echo "Executing script to build swagger-spec.yaml, downloading model definitions from git."
echo "Github account user name?"
read username
echo "Github account password?"
read password
echo "Branch to download?"
read branch

SWAGGER_PATH=$SRC_PATH/src/main/resources/swagger
SWAGGER_PATHS_DIR=$SWAGGER_PATH/paths
SWAGGER_TEMPLATE_PATH=$SWAGGER_PATH/swagger-template.txt

python Download.py $username $password $branch $SWAGGER_PATH $SWAGGER_PATHS_DIR $SWAGGER_TEMPLATE_PATH models

echo "Successfully built swagger-spec.yaml file(s), removing build script."

rm -f Download.py

mvn clean install -X

echo "SUCCESS"
