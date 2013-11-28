#!/bin/sh
set -x

find albums/ -name \*thumbnail.JPG | xargs rm 
find albums/ -name \*meta\* | xargs rm 
