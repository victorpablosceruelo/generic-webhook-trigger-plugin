#!/bin/bash

if [ -z "${1}" ] || [ "" == "${1}" ]; then
    echo " "
    echo "Usage: $(basename $0) testFileName http://JENKINS_URL/generic-webhook-trigger/invoke "
    echo "Examples: "
    echo "    $(basename $0) ex_merge_test01.json http://localhost:8080/jenkins/generic-webhook-trigger/invoke "
    echo "    $(basename $0) ex_merge_test02.json http:/localhost:8080/jenkins/my-gitlab-webhook-trigger/invoke "
    echo " "
    ls -la "`pwd`"
    exit 0
fi

TEST_FILENAME="$1"
URL="$2"
MY_PWD="`pwd`"

cat > /tmp/example01.json <<EOF
example contents ...
EOF

# curl --insecure -H "Content-Type: application/json" --data @/tmp/example.json ${URL}
echo "curl --insecure -H "Content-Type: application/json" --data @${MY_PWD}/${TEST_FILENAME} ${URL} "
curl --insecure -H "Content-Type: application/json" --data @${MY_PWD}/${TEST_FILENAME} ${URL}
echo "RetVal: $?"
