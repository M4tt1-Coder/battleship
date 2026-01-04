#!/usr/bin/env python3
import os
import subprocess
import sys


def is_windows():
    return sys.platform.startswith("win") or os.name == "nt"


def main():
    if is_windows():
        # On Windows, including Cygwin and MSYS, run mvnw.cmd
        command = ["mvnw.cmd", "com.spotify.fmt:fmt-maven-plugin:format"]
    else:
        # On Unix-like systems, run ./mvnw
        command = ["./mvnw", "com.spotify.fmt:fmt-maven-plugin:format"]

    try:
        subprocess.run(command, check=True)
    except subprocess.CalledProcessError as e:
        print(f"Error executing command: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
