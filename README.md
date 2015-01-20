# mad-java
Java based modular audio prototyping application

# Building And Running
Currently development is done using Eclipse and JDK 1.8 on a Linux machine with Jack 2.

1. Check out using git.

2. Pull down the libraries it depends on using the shell script (it will need wget in your path):
```
cd mad-java/4EXTERNAL/external-libs/scripts
./fetchandextractlibs.sh
```

This will populate the libs directory as needed.

3. Add the git repository you just cloned int the eclipse git perspective

4. Import all the projects it finds under there.

5. We need to generate some jaxb marshalling code in the rack-marshalling-jaxb project - launch the ant build.xml found in the project.

6. Refresh, things should start building.

7. Build the native library for getting the thread ID in util-native. You will need to make sure that your JAVA_HOME environment is correctly set up.

```
cd $CHECKOUTDIR/3UTIL/util-native/csrc
./buildit.sh
```

This should place it where eclipse will pick it up and use it.

7. Launch the component-designer java application - uk.co.modularaudio.componentdesigner.ComponentDesigner
