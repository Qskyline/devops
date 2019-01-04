#/bin/bash
#set -x

#params
#定义仓库地址(必选)
repo_url=ssh://git@github.com:Qskyline/client.git
#指定构建版本(必选)
branch=devops
#指定构建生成目录(必选,基于构建目录的相对路径)
generate_dir=dist
#指定构建目录(可选)
build_dir=/Users/skyline/WebstormProjects
#指定构建输出路径(可选,绝对路径)
output_dir=

#外部传入params覆盖定义值
if [ -n "$1" ]; then
    repo_url=$1
fi
if [ -n "$2" ]; then
    branch=$2
fi
if [ -n "$3" ]; then
    generate_dir=$3
fi
if [ -n "$4" ]; then
    build_dir=$4
fi
if [ -n "$5" ]; then
    output_dir=$5
fi

#记录当前目录
current_workdir=$(pwd)

#判断必须params是否为空
if [ -z "$repo_url" ]; then
    echo 'Must specify the repo_url!'
    exit
fi
if [ -z "$branch" ]; then
    echo 'Must specify the branch!'
    exit
fi
if [ -z "$generate_dir" ]; then
    echo 'Must specify the generate_dir!'
    exit
fi

#计算项目名
temp=${repo_url##*/}
prj_name=${temp%.*}

#为当前依然为空的可选参数赋默认值
if [ -z "$build_dir" ]; then
    build_dir=/tmp
fi
if [ -z "$output_dir" ]; then
    output_dir=$current_workdir/src/main/resources/static
fi

#拉取项目并进入项目目录
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

#记录git当前所在分支
current_branch=$(git branch | grep '*' | awk '{print $2}')

#基于指定分支创建构建分支
build_branch=$branch"-build-"$(date "+%Y%m%d%H%M")

#切换到构建分支
git checkout -b $build_branch origin/$branch
git pull

#构建
npm install
npm run build

#放置构建包到输出目录
if [ ! -d $output_dir ]; then
    mkdir -p $output_dir
fi
rm -fr $output_dir/*
cp -fr $generate_dir/* $output_dir/

#切换回构建前的分支
git checkout $current_branch

#删除临时的构建分支
git branch -d $build_branch

#回到执行此脚本时的目录
cd $current_workdir