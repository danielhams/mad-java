#!/bin/sh

#############################################
# Component Designer launcher configuration #
#############################################

# Where your JDK lives (1.8 please, 1.7 has performance issues with swing)
#export JAVA_HOME=/home/dan/Development/Jdks/jdk1.8.0_45
#export JAVA_HOME=/home/dan/Development/Jdks/jdk1.8.0_45
if [ "$JAVA_HOME"q == "q" ]; then
    echo "You must set JAVA_HOME before running this script"
    exit 1;
fi

# Whether to use the java platform look and feel
# Values: yes or no
export USE_NATIVE_LAF=yes
#export USE_NATIVE_LAF=no

# What components you can select from the GUI
# Values: --alpha (show all) or --beta (show beta + released)
#export DEVELOPMENT_COMPONENTS=--alpha
#export DEVELOPMENT_COMPONENTS=--beta

# Use an additional jar as a source of components
# The jar must be called cd_plugins.jar
# and must have a pluginsbeans.xml at the root
# and a pluginsconfiguration.properties at the root
# Values: yes or no
export PLUGIN_JAR=no

#############################################
# Shouldn't need to change below here       #
#############################################

export CD_DIR=`pwd`

export CD_NATIVE_LIB_PATH=$CD_DIR/natives

export LD_LIBRARY_PATH=$CD_NATIVE_LIB_PATH:$LD_LIBRARY_PATH
export DYLD_LIBRARY_PATH=$CD_NATIVE_LIB_PATH:$DYLD_LIBRARY_PATH

export AA_OPTIONS="-Dawt.useSystemAAFontSettings=on -Dswing.aatext=true"

#export GL_OPTIONS="-Dsun.java2d.opengl=True"

#export VM_MEM_OPTS="-Xms256m -Xmx512m"
#export VM_MEM_OPTS="-Xms512m -Xmx512m"
#export VM_MEM_OPTS="-Xms512m -Xmx1024m"

#export VM_GC_OPTS="-Xincgc"
#export VM_GC_OPTS="-XX:+UseG1GC"
#export VM_GC_OPTS="-XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=80 -XX:MaxGCPauseMillis=10"
export VM_GC_OPTS="-XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=50 -XX:MaxGCPauseMillis=10"
#export VM_GC_OPTS="-XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=10 -XX:MaxGCPauseMillis=5"
#export VM_GC_OPTS="-XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:CMSInitiatingOccupancyFraction=80"
#export VM_GC_DEBUG="-XX:+PrintGC"
#export VM_GC_DEBUG="-XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:gclogfile.gc"

export VM_PROPS="-Dorg.jboss.logging.provider=slf4j"

# Comment this out if you want to use visualvm etc
export VM_DISABLE_PERF_DATA="-XX:-UsePerfData"

export CD_JAR=component-designer-0.0.3.jar
export SL_DIR=supportlibs

export CLASSPATH=$(echo supportlibs/*.jar |tr ' ' ':')
export MAINJAR=$(echo *.jar |tr ' ' ':')
export CLASSPATH=$MAINJAR:$CLASSPATH

export CD_SWITCHES=""

if [ "$USE_NATIVE_LAF"q == "yesq" ]; then
    export CD_SWITCHES="$CD_SWITCHES --useSlaf"
fi

if [ "$DEVELOPMENT_COMPONENTS"q != "q" ]; then
    export CD_SWITCHES="$CD_SWITCHES $DEVELOPMENT_COMPONENTS"
fi

if [ "$PLUGIN_JAR"q == "yesq" ]; then
    export CLASSPATH=$CLASSPATH:cdplugin.jar
    export CD_SWITCHES="$CD_SWITCHES --pluginJar"
fi

export JAVA_CL_ARGS="$AA_OPTIONS $GL_OPTIONS $VM_PROPS $VM_MEM_OPTS $VM_GC_OPTS $VM_GC_DEBUG $VM_DISABLE_PERF_DATA uk.co.modularaudio.componentdesigner.ComponentDesignerLauncher $CD_SWITCHES"

echo "Running $JAVA_HOME/bin/java $JAVA_CL_ARGS"
$JAVA_HOME/bin/java $JAVA_CL_ARGS
