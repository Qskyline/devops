#/bin/bash

branch=devops
if [ -n "$1" ]; then
    branch=$1
fi

build_dir=/Users/skyline/WebstormProjects
if [ -n "$2" ]; then
    build_dir=$2
fi

workdir=$(cd `dirname $0`; pwd)
repo_url=ssh://git@github.com:Qskyline/client.git
temp=${repo_url##*/}
prj_name=${temp%.*}

if [ -d "$build_dir/$prj_name" ]; then
    cd $build_dir/$prj_name
elif [ -d "$build_dir" ]; then
    cd $build_dir
    git clone $repo_url
    cd $prj_name
else
    mkdir -p $build_dir
    cd $build_dir
    git clone $repo_url
    cd $prj_name
fi

current_branch=$(git branch | grep '*' | awk '{print $2}')
build_branch=$branch"-build-"$(date "+%Y%m%d%H%M")
git checkout -b $build_branch origin/$branch

npm install
npm run build

rm -fr $workdir/src/main/resources/static/*
cp -fr dist/* $workdir/src/main/resources/static/

git checkout $current_branch
git branch -d $build_branch
cd $workdir