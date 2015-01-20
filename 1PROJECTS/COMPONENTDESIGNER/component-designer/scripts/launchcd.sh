#!/bin/sh

#############################################
# Component Designer launcher configuration #
#############################################

# Where your JDK lives (1.8 please, 1.7 has performance issues with swing)
export JAVA_HOME=/home/dan/Development/Jdks/jdk1.8.0_25

# Whether to use the java platform look and feel
# Values: yes or no
export USE_NATIVE_LAF=yes
#export USE_NATIVE_LAF=no

# What components you can select from the GUI
# Values: --alpha (show all) or --beta (show beta + released)
export DEVELOPMENT_COMPONENTS=--alpha
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

export CD_NATIVE_LIB_PATH=$CD_DIR/nativelib

export LD_LIBRARY_PATH=$CD_NATIVE_LIB_PATH:$LD_LIBRARY_PATH

#export VM_MEM_OPTS="-Xms512m -Xmx1024m"
export VM_GC_OPTS="-XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=80 -XX:MaxGCPauseMillis=10"
#export VM_GC_DEBUG="-XX:+PrintGC"

# Comment this out if you want to use visualvm etc
#export VM_DISABLE_PERF_DATA="-XX:-UsePerfData"

export CLASSPATH=$CD_DIR/cd.jar:$CD_DIR/cd_lib:$CLASSPATH

export CD_SWITCHES=
if [ "$USE_NATIVE_LAF"q == "yesq" ]; then
    export CD_SWITCHES="$CD_SWITCHES --useSlaf"
fi

if [ "$DEVELOPMENT_COMPONENTS"q != "q" ]; then
    export CD_SWITCHES="$CD_SWITCHES $DEVELOPMENT_COMPONENTS"
fi

if [ "$PLUGIN_JAR"q == "yesq" ]; then
    export CLASSPATH=$CD_DIR/cdplugin.jar:$CLASSPATH
    export CD_SWITCHES="$CD_SWITCHES --pluginJar"
fi

export JAVA_CL_ARGS="-cp $CLASSPATH $VM_MEM_OPTS $VM_GC_OPTS $VM_GC_DEBUG $VM_DISABLE_PERF_DATA uk.co.modularaudio.componentdesigner.ComponentDesigner $CD_SWITCHES"

echo "Running $JAVA_HOME/bin/java $JAVA_CL_ARGS"
$JAVA_HOME/bin/java $JAVA_CL_ARGS
