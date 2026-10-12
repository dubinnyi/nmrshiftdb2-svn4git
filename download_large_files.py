#!/bin/python

import os
import subprocess

def download_from_gdrive():
    # Check that gdown is installed
    try:
        import gdown
    except ImportError:
        print("Installing gdown...")
        subprocess.check_call(["pip", "install", "gdown"])
        import gdown

    # Create folders
    os.makedirs("nmrshiftdb2-tests", exist_ok=True)
    os.makedirs("snapshots", exist_ok=True)

    print("Downloading folder nmrshiftdb2-tests...")
    # Ссылка на папку nmrshiftdb2-tests (или файл nmrshiftdb.sql.gz внутри нее)
    # Folder ID is: 1JQHOxQAc4Asyu8Fv3yAHbwIm5urXQBj
    gdown.download_folder(id='1JQHOxQAc4Asyu8Fv3yAHbwIm5urXQBj', output='nmrshiftdb2-tests', quiet=False)

    print("Downloading folder snapshots...")
    # Folder ID is: 1BbuwK_YqdfqKSCiKXR-_SO3C4DXaYqnh
    gdown.download_folder(id='1BbuwK_YqdfqKSCiKXR-_SO3C4DXaYqnh', output='snapshots', quiet=False)

    print("All files are downloaded!")

if __name__ == "__main__":
    download_from_gdrive()
