#!/usr/bin/env bash
#

MAVEN_SNAPSHOT_REPOSITORY="https://central.sonatype.com/repository/maven-snapshots"

TAG_DOWNLOAD_SNAPSHOT="download-snapshot"
README_FILE="README.md"
INSTALLATION_FILE="./docs/source/gettingstarted/gettingstarted.md"

function isSnapshot() {
	if [[ "${VERSION}" =~ -SNAPSHOT$ ]]; then
		return 0
	else
		return 1
	fi
}

# 1: a string seperated by . like groupId
function toUrlPath() {
	echo "${1//./\/}"
}

# 1: tag
function startTag()
{
	echo "<!--start:$1-->\n"
}

# 1: tag
function endTag()
{
	echo "<!--end:$1-->"
}

function escapeForSed() {
    printf '%s\n' "$1" | sed 's|[\/&\(\)\!`]|\\&|g;'
}

# 1: file
# 2: tag
# 3: newValue
# 4: originalValue(optional, default: matches anything)
function replaceValue()
{
	local file="$1"
	local tag="$2"
	local newValue=$(escapeForSed "$3")
	local originalValue=$(escapeForSed "${4:-.*}")
	local startTag=$(escapeForSed "$(startTag "$tag")")
	local endTag=$(escapeForSed "$(endTag "$tag")")
	local command="s|$startTag($originalValue)$endTag|$startTag$newValue$endTag|g"
	eval "sed -r -z \"$command\" -i \"$file\""
}

GROUP_ID=$(mvn help:evaluate -Dexpression=project.groupId -q -DforceStdout)
ARTIFACT_ID=$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout)
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

baseUrl="${MAVEN_SNAPSHOT_REPOSITORY}/$(toUrlPath $GROUP_ID)/$(toUrlPath $ARTIFACT_ID)/${VERSION}"
echo "Trying to determine latest snapshot release for ${GROUP_ID}:${ARTIFACT_ID}:${VERSION}..."
metadataUrl="${baseUrl}/maven-metadata.xml"
metadata=$(curl -s ${metadataUrl})
if [ $? -ne 0 ]; then
    echo "Error fetching metadata from Maven Snapshot Repository (url: ${metadataUrl})"
    exit 1
fi
timestamp=$(echo "$metadata" | grep -oP '(?<=<timestamp>).*?(?=</timestamp>)')
buildNumber=$(echo "$metadata" | grep -oP '(?<=<buildNumber>).*?(?=</buildNumber>)')
echo "Found latest snapshot (timestamp: ${timestamp}, buildNumber: ${buildNumber})"
downloadUrl="${baseUrl}/${ARTIFACT_ID}-${VERSION:0:-9}-${timestamp}-${buildNumber}.jar"
echo "Setting new download URL for latest snapshot release: ${downloadUrl}"
replaceValue "$README_FILE" "$TAG_DOWNLOAD_SNAPSHOT" "[Download latest SNAPSHOT version ($VERSION)](${downloadUrl})"
replaceValue "$INSTALLATION_FILE" "$TAG_DOWNLOAD_SNAPSHOT" "{download}\`Latest SNAPSHOT version ($VERSION) <${downloadUrl}>\`"
echo "Latest snapshot download URL updated successfully"
