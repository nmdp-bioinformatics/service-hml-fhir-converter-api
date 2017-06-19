#!/bin/sh

SRC_PATH=`pwd`

while getopts ":u:p:b:m:y:" opt; do
    case $opt in
        u) username="$OPTARG"
        ;;
        p) password="$OPTARG"
        ;;
        b) branch="$OPTARG"
        ;;
        m) model_path="$OPTARG"
        ;;
        y) python_script="$OPTARG"
        ;;
    esac
done

if [ -z "$username" ]; then
    echo "Github username?"
    read username
fi

if [ -z "$password" ]; then
    echo "Github password?"
    read password
fi

if [ -z "$branch" ]; then
    echo "Github branch?"
    read branch
fi

if [ -z "$python_script" ]; then
    echo "Importing model building script from git."
    curl -LJO https://raw.githubusercontent.com/nmdp-bioinformatics/util-swagger-codegen-models/master/Download.py
    echo "Executing script to build swagger-spec.yaml, downloading model definitions from git."
fi

SWAGGER_PATH=$SRC_PATH/src/main/resources/swagger
SWAGGER_PATHS_DIR=$SWAGGER_PATH/paths
SWAGGER_TEMPLATE_PATH=$SWAGGER_PATH/swagger-template.txt

python Download.py $username $password $branch $SWAGGER_PATH $SWAGGER_PATHS_DIR $SWAGGER_TEMPLATE_PATH models $model_path

echo "Successfully built swagger-spec.yaml file(s), removing build script."

rm -f Download.py

mvn clean install -X

echo "SUCCESS"
