#!/bin/bash

# Installing gdown
pip install gdown

# Create folders
mkdir -p nmrshiftdb2-tests snapshots

# Download content of the folder nmrshiftdb2-tests
gdown --folder "https://drive.google.com/drive/folders/1JQHOxQAc4Asyu8Fv3yAHbwIm5urXQBj" -O nmrshiftdb2-tests/

# Download content of the folder snapshots
gdown --folder "https://drive.google.com/drive/folders/1BbuwK_YqdfqKSCiKXR-_SO3C4DXaYqnh" -O snapshots/
