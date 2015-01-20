# mad-java
Java based modular audio prototyping application

# Building
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

5.
