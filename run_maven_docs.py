"""
Module Name: run_maven_docs
Author: Matthis Geißler
Email: matthisgeissler@gmail.com
Last Modified: 2026-02-01
Description:
    Generates javadoc HTML documentation files for the project.
"""

#!/usr/bin/env python3
import os
import subprocess
import sys


def is_windows():
    """
    Checks if the script is run on a Windows machine of not.

    # Returns

    => TRUE, if we are on Windows
    """
    return sys.platform.startswith("win") or os.name == "nt"


def main():
    """
    Entrypoint for the script!
    """
    if is_windows():
        # On Windows, including Cygwin and MSYS, run mvnw.cmd
        command = ["mvnw.cmd", "clean" , "compile", "javadoc:javadoc"]
    else:
        # On Unix-like systems, run ./mvnw
        command = ["./mvnw", "clean", "compile" ,"javadoc:javadoc"]

    try:
        subprocess.run(command, check=True)
    except subprocess.CalledProcessError as e:
        print(f"Error executing command: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
