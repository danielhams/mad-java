# Mad Java
A Java based modular audio prototyping application that allows wiring of audio components together.

# Screenshots
<img src="1PROJECTS/COMPONENTDESIGNER/component-designer/screenshots/madjava001-playingasoundfile.png">Playing a soundfile</img>
<img src="1PROJECTS/COMPONENTDESIGNER/component-designer/screenshots/madjava001-bassboostrack.png">A rack boosting the bass of a signal</img>
<img src="1PROJECTS/COMPONENTDESIGNER/component-designer/screenshots/madjava001-bassboostwiring.png">The wiring behind the bass booster</img>

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

7. Launch the component-designer java application - uk.co.modularaudio.componentdesigner.ComponentDesigner - but we'll need to pass some arguments to the application so it find the necessary image and support files.

   ```
   ComponentDesigner --development
   ```

   Don't forget that you will need to ensure that Jack2 is already running before launching.

8. You should see the ComponentDesigner main window and the initially empty rack.

9. You can now add (for example) a sound file player component to the rack, and wire its output to the output in the master IO at the top. Don't forget to wire up the outputs inside the Jack manager you are using too (e.g. QJackctl).

10. A couple of example racks can be found in:
    1PROJECTS/COMPONENTDESIGNER/component-designer/userpatches

    And example sub-racks in:
    1PROJECTS/COMPONENTDESIGNER/component-designer/usersubpatches

11. Further tweaking - you can also add the following switches:

    | switch    | description                                                                             |
    |-----------|-----------------------------------------------------------------------------------------|
    | --useSlaf | use the gtk2 style platform look and feel. works best with a dark one like dark-adwaita |
    | --beta    | show released and beta level DSP components                                             |
    | --alpha   | show all components include alpha and beta ones                                         |
