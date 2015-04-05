#!/bin/bash

export CURDIR=`pwd`
export BASEDIR=$CURDIR/..
export OUTDIR=$BASEDIR/libs
export TMPDIR=$BASEDIR/tmp
export EXTRACTDIR=$TMPDIR/extractdir

echo "Root of libraries file is $BASEDIR"

mkdir -p $TMPDIR
mkdir -p $EXTRACTDIR
mkdir -p $OUTDIR

extractZip()
{
    zipFile=$1;
    cwd=`pwd`
    cd $EXTRACTDIR
    unzip -n ../$zipFile
    if [ $? != 0 ]; then
        echo "Failed during extraction of $zipFile"
        exit -1;
    fi
    cd $cwd
}

extractTargz()
{
    targzFile=$1
    cwd=`pwd`
    cd $EXTRACTDIR
    tar zxf ../$targzFile
    if [ $? != 0 ]; then
        echo "Failed during extraction of $targzFile"
        exit -1
    fi
    cd $cwd
}

fetchAndExtractOneLibrary()
{
    libraryUrl=$1
    outputFile=$2
    extractionType=$3

    if [ -f $TMPDIR/$outputFile ]; then
        echo "Found existing library $outputFile. Skipping download."
        return
    fi
    
    wget -c -O $TMPDIR/$outputFile $libraryUrl
    if [ $? != 0 ]; then
        echo "Failed downloading $libraryUrl"
        exit -1
    fi

    if [ "$extractionType"q == "targz"q ]; then
        extractTargz $outputFile
    elif [ "$extractionType"q == "zip"q ]; then
        extractZip $outputFile
    fi
}

fetchOneLibraryDirectRename()
{
    libraryUrl=$1
    outputFile=$2

    if [ -f $OUTDIR/$outputFile ]; then
        echo "Found existing library $outputFile. Skipping download."
        return
    fi
    
    cwd=`pwd`
    cd $OUTDIR
    wget -c -O $outputFile $libraryUrl
    if [ $? != 0 ]; then
        echo "Failed downloading $libraryUrl"
        exit -1
    fi
    cd $cwd
}

copyNeededLib()
{
    neededLib=$1
    cp -n $EXTRACTDIR/$neededLib $OUTDIR/
    if [ $? != 0 ]; then
        echo "Unable to copy needed library: $neededLib"
        exit -1
    fi
}

# log4j
fetchAndExtractOneLibrary "http://www.mirrorservice.org/sites/ftp.apache.org/logging/log4j/1.2.17/log4j-1.2.17.tar.gz" "log4j-1.2.17.tar.gz" "targz"
copyNeededLib "apache-log4j-1.2.17/log4j-1.2.17.jar"

# commons logging
fetchAndExtractOneLibrary "http://www.mirrorservice.org/sites/ftp.apache.org//commons/logging/binaries/commons-logging-1.2-bin.tar.gz" "commons-logging-1.2-bin.tar.gz" "targz"
copyNeededLib "commons-logging-1.2/commons-logging-1.2.jar"

# commons codec
fetchAndExtractOneLibrary "http://www.mirrorservice.org/sites/ftp.apache.org//commons/codec/binaries/commons-codec-1.10-bin.tar.gz" "commons-codec-1.10-bin.tar.gz" "targz"
copyNeededLib "commons-codec-1.10/commons-codec-1.10.jar"

# apache mahout
fetchAndExtractOneLibrary "http://archive.apache.org/dist/mahout/0.9/mahout-distribution-0.9.tar.gz" "mahout-distribution-0.9.tar.gz" "targz"
copyNeededLib "mahout-distribution-0.9/mahout-core-0.9.jar"
copyNeededLib "mahout-distribution-0.9/mahout-math-0.9.jar"

# spring framework
fetchAndExtractOneLibrary "http://maven.springframework.org/release/org/springframework/spring/4.1.4.RELEASE/spring-framework-4.1.4.RELEASE-dist.zip" "spring-framework-4.1.4.RELEASE-dist.zip" "zip"
copyNeededLib "spring-framework-4.1.4.RELEASE/libs/spring-core-4.1.4.RELEASE.jar"
copyNeededLib "spring-framework-4.1.4.RELEASE/libs/spring-context-4.1.4.RELEASE.jar"
copyNeededLib "spring-framework-4.1.4.RELEASE/libs/spring-beans-4.1.4.RELEASE.jar"
copyNeededLib "spring-framework-4.1.4.RELEASE/libs/spring-expression-4.1.4.RELEASE.jar"

# hibernate
fetchAndExtractOneLibrary "http://netcologne.dl.sourceforge.net/project/hibernate/hibernate4/4.3.7.Final/hibernate-release-4.3.7.Final.tgz" "hibernate-release-4.3.7.Final.tgz" "targz"
copyNeededLib "hibernate-release-4.3.7.Final/lib/required/hibernate-core-4.3.7.Final.jar"
copyNeededLib "hibernate-release-4.3.7.Final/lib/required/dom4j-1.6.1.jar"
copyNeededLib "hibernate-release-4.3.7.Final/lib/required/hibernate-commons-annotations-4.0.5.Final.jar"
copyNeededLib "hibernate-release-4.3.7.Final/lib/required/jboss-logging-3.1.3.GA.jar"
copyNeededLib "hibernate-release-4.3.7.Final/lib/required/jboss-transaction-api_1.2_spec-1.0.0.Final.jar"
copyNeededLib "hibernate-release-4.3.7.Final/lib/required/hibernate-jpa-2.1-api-1.0.0.Final.jar"
copyNeededLib "hibernate-release-4.3.7.Final/lib/required/javassist-3.18.1-GA.jar"
copyNeededLib "hibernate-release-4.3.7.Final/lib/required/antlr-2.7.7.jar"

# hsqldb
fetchAndExtractOneLibrary "http://heanet.dl.sourceforge.net/project/hsqldb/hsqldb/hsqldb_2_3/hsqldb-2.3.2.zip" "hsqldb-2.3.2.zip" "zip"
copyNeededLib "hsqldb-2.3.2/hsqldb/lib/hsqldb.jar"

# miglayout swing
fetchOneLibraryDirectRename "https://oss.sonatype.org/content/repositories/releases/com/miglayout/miglayout-core/5.0/miglayout-core-5.0.jar" "miglayout-core-5.0.jar"
fetchOneLibraryDirectRename "https://oss.sonatype.org/content/repositories/releases/com/miglayout/miglayout-swing/5.0/miglayout-swing-5.0.jar" "miglayout-swing-5.0.jar"

# jtransforms
fetchOneLibraryDirectRename "http://search.maven.org/remotecontent?filepath=com/github/wendykierp/JTransforms/3.0/JTransforms-3.0.jar" "JTransforms-3.0.jar"

# Jaxbi RI
fetchAndExtractOneLibrary "https://jaxb.java.net/2.2.11/jaxb-ri-2.2.11.zip" "jaxb-ri-2.2.11.zip" "zip"
copyNeededLib "jaxb-ri/lib/jaxb-api.jar"
copyNeededLib "jaxb-ri/lib/jaxb-core.jar"
copyNeededLib "jaxb-ri/lib/jaxb-impl.jar"
copyNeededLib "jaxb-ri/lib/jaxb-jxc.jar"
copyNeededLib "jaxb-ri/lib/jaxb-xjc.jar"

# JNA
fetchAndExtractOneLibrary "https://codeload.github.com/twall/jna/tar.gz/4.1.0" "jna-4.1.0.tar.gz" "targz"
copyNeededLib "jna-4.1.0/dist/jna.jar"

# JNAJack
# We currently need some features that aren't in this library yet, use a custom version I've been working with
#fetchOneLibraryDirectRename "jnajack-custom.jar" "jnajack-custom.jar"

# Remove any .project files we find under the extract dir so that eclipse doesn't try and import them
find $EXTRACTDIR -name ".project" -exec rm {} \;

exit
