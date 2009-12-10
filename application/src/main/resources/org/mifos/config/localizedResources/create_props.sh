#!/bin/bash
set -o errexit

# creates locale-specific .properties files for use by Mifos web app.

sourceDir=$1
targetDir=$2

if [ -z "$sourceDir" ] || [ -z "$targetDir" ]; then
    echo "Usage: $0 SOURCE_DIR TARGET_DIR"
    exit 1
fi

if [ ! -d $targetDir ] || [ ! -d $sourceDir ]; then
    echo "ERROR: SOURCE_DIR and TARGET_DIR must both be directories"
    exit 1
fi

localeDirs=`find $sourceDir -mindepth 1 -maxdepth 1 -regex '.*/[a-zA-Z_]+$' -type d`
if [ -z "$localeDirs" ]; then
    echo "ERROR: no locale directories found in $sourceDir"
    exit 1
fi

for localeDir in $localeDirs
do
    locale=`basename $localeDir`
    for translated in `find $localeDir -type f -name "*.po"`
    do
        # remove directory part of path
        translatedBase=`basename $translated`

        # remove file extension
        bundleBase=`echo $translatedBase | sed -e 's/\.po$//'`

        # create locale-specific .properties files for use by Mifos web app
        po2prop -t $sourceDir/$bundleBase.properties \
            $translated $targetDir/${bundleBase}_$locale.properties
    done
done

if ! find $targetDir -type f | grep --quiet .; then
    echo "ERROR: no translations found in $targetDir. Template Toolkit probably failed."
    exit 1
fi
