#!/usr/bin/env bash

while getopts ":u:p:b:m:y:v:" opt; do
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
        v) mvn_version="$OPTARG"
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

if [ -z "$model_path" ]; then
    model_path=
fi

if [ -z "$model_path" ] && [ -z "$python_script" ]; then
    sh build.sh -u $username -p $password -b $branch
elif [ ! -z "$model_path" ] && [ -z "$python_script" ]; then
    sh build.sh -u $username -p $password -b $branch -m $model_path
elif [ ! -z "$python_script" ] && [ -z "$model_path" ]; then
    sh build.sh -u $username -p $password -b $branch -y $python_script
elif [ -z "$python_script" ] && [ -z "$model_path" ]; then
    sh build.sh -u $username -p $password -b $branch -m $model_path -y $python_script
fi

mvn install:install-file -Dfile=target/service-hml-fhir-converter-api-1.1.0-SNAPSHOT.jar

echo "Successfully installed to maven repo."
