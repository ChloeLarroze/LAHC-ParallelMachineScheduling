#!/bin/bash

#compil
javac --module-path ./javafx-sdk-11.0.2/lib \
      --add-modules javafx.controls,javafx.fxml \
      -d bin \
      $(find . -name "*.java")

#run
java --module-path ./javafx-sdk-11.0.2/lib \
     --add-modules javafx.controls,javafx.fxml \
     -Dprism.order=sw \
     -cp bin gui.App

