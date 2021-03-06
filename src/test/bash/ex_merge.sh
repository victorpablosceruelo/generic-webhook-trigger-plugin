#!/bin/bash

if [ -z "${1}" ] || [ "" == "${1}" ]; then
    echo "Usage: $(basename $0) http://JENKINS_URL/generic-webhook-trigger/invoke "
    echo "Example: $(basename $0) http://localhost:8080/jenkins/generic-webhook-trigger/invoke "
    exit 0
fi

URL="$1"

cat > /tmp/example.json <<EOF
 
{
  "object_kind": "merge_request",
  "event_type": "merge_request",
  "user": {
    "name": "Administrator",
    "username": "root",
    "avatar_url": "https://secure.gravatar.com/avatar/e64c7d89f26bd1972efa854d13d7dd61?s=80&d=identicon"
  },
  "project": {
    "id": 108,
    "name": "piloto",
    "description": "",
    "web_url": "https://des-gitlab.scae.redsara.es/JAVA/OTP-IC/Piloto-IC/piloto",
    "avatar_url": null,
    "git_ssh_url": "git@des-gitlab.scae.redsara.es:JAVA/OTP-IC/Piloto-IC/piloto.git",
    "git_http_url": "https://des-gitlab.scae.redsara.es/JAVA/OTP-IC/Piloto-IC/piloto.git",
    "namespace": "Piloto-IC",
    "visibility_level": 0,
    "path_with_namespace": "JAVA/OTP-IC/Piloto-IC/piloto",
    "default_branch": "master",
    "ci_config_path": null,
    "homepage": "https://des-gitlab.scae.redsara.es/JAVA/OTP-IC/Piloto-IC/piloto",
    "url": "git@des-gitlab.scae.redsara.es:JAVA/OTP-IC/Piloto-IC/piloto.git",
    "ssh_url": "git@des-gitlab.scae.redsara.es:JAVA/OTP-IC/Piloto-IC/piloto.git",
    "http_url": "https://des-gitlab.scae.redsara.es/JAVA/OTP-IC/Piloto-IC/piloto.git"
  },
  "object_attributes": {
    "assignee_id": null,
    "author_id": 1,
    "created_at": "2020-01-20 12:47:35 UTC",
    "description": "",
    "head_pipeline_id": null,
    "id": 181,
    "iid": 16,
    "last_edited_at": null,
    "last_edited_by_id": null,
    "merge_commit_sha": null,
    "merge_error": null,
    "merge_params": {
      "force_remove_source_branch": "0"
    },
    "merge_status": "unchecked",
    "merge_user_id": null,
    "merge_when_pipeline_succeeds": false,
    "milestone_id": null,
    "source_branch": "developer",
    "source_project_id": 109,
    "state_id": 1,
    "target_branch": "integracion",
    "target_project_id": 108,
    "time_estimate": 0,
    "title": "Update pom.xml",
    "updated_at": "2020-01-20 12:47:35 UTC",
    "updated_by_id": null,
    "url": "https://des-gitlab.scae.redsara.es/JAVA/OTP-IC/Piloto-IC/piloto/merge_requests/16",
    "source": {
      "id": 109,
      "name": "piloto",
      "description": "",
      "web_url": "https://des-gitlab.scae.redsara.es/JAVA/OTP/Piloto/piloto",
      "avatar_url": null,
      "git_ssh_url": "git@des-gitlab.scae.redsara.es:JAVA/OTP/Piloto/piloto.git",
      "git_http_url": "https://des-gitlab.scae.redsara.es/JAVA/OTP/Piloto/piloto.git",
      "namespace": "Piloto",
      "visibility_level": 0,
      "path_with_namespace": "JAVA/OTP/Piloto/piloto",
      "default_branch": "master",
      "ci_config_path": null,
      "homepage": "https://des-gitlab.scae.redsara.es/JAVA/OTP/Piloto/piloto",
      "url": "git@des-gitlab.scae.redsara.es:JAVA/OTP/Piloto/piloto.git",
      "ssh_url": "git@des-gitlab.scae.redsara.es:JAVA/OTP/Piloto/piloto.git",
      "http_url": "https://des-gitlab.scae.redsara.es/JAVA/OTP/Piloto/piloto.git"
    },
    "target": {
      "id": 108,
      "name": "piloto",
      "description": "",
      "web_url": "https://des-gitlab.scae.redsara.es/JAVA/OTP-IC/Piloto-IC/piloto",
      "avatar_url": null,
      "git_ssh_url": "git@des-gitlab.scae.redsara.es:JAVA/OTP-IC/Piloto-IC/piloto.git",
      "git_http_url": "https://des-gitlab.scae.redsara.es/JAVA/OTP-IC/Piloto-IC/piloto.git",
      "namespace": "Piloto-IC",
      "visibility_level": 0,
      "path_with_namespace": "JAVA/OTP-IC/Piloto-IC/piloto",
      "default_branch": "master",
      "ci_config_path": null,
      "homepage": "https://des-gitlab.scae.redsara.es/JAVA/OTP-IC/Piloto-IC/piloto",
      "url": "git@des-gitlab.scae.redsara.es:JAVA/OTP-IC/Piloto-IC/piloto.git",
      "ssh_url": "git@des-gitlab.scae.redsara.es:JAVA/OTP-IC/Piloto-IC/piloto.git",
      "http_url": "https://des-gitlab.scae.redsara.es/JAVA/OTP-IC/Piloto-IC/piloto.git"
    },
    "last_commit": {
      "id": "d7ca05ed26fb2970cf687a80d61f7bd97874e359",
      "message": "Update pom.xml",
      "timestamp": "2020-01-20T13:47:11+01:00",
      "url": "https://des-gitlab.scae.redsara.es/JAVA/OTP-IC/Piloto-IC/piloto/commit/d7ca05ed26fb2970cf687a80d61f7bd97874e359",
      "author": {
        "name": "Administrator",
        "email": "admin@example.com"
      }
    },
    "work_in_progress": false,
    "total_time_spent": 0,
    "human_total_time_spent": null,
    "human_time_estimate": null,
    "assignee_ids": [

    ],
    "state": "opened",
    "action": "open"
  },
  "labels": [

  ],
  "changes": {
    "author_id": {
      "previous": null,
      "current": 1
    },
    "created_at": {
      "previous": null,
      "current": "2020-01-20 12:47:35 UTC"
    },
    "description": {
      "previous": null,
      "current": ""
    },
    "id": {
      "previous": null,
      "current": 181
    },
    "iid": {
      "previous": null,
      "current": 16
    },
    "merge_params": {
      "previous": {
      },
      "current": {
        "force_remove_source_branch": "0"
      }
    },
    "source_branch": {
      "previous": null,
      "current": "developer"
    },
    "source_project_id": {
      "previous": null,
      "current": 109
    },
    "target_branch": {
      "previous": null,
      "current": "integracion"
    },
    "target_project_id": {
      "previous": null,
      "current": 108
    },
    "title": {
      "previous": null,
      "current": "Update pom.xml"
    },
    "updated_at": {
      "previous": null,
      "current": "2020-01-20 12:47:35 UTC"
    },
    "total_time_spent": {
      "previous": null,
      "current": 0
    }
  },
  "repository": {
    "name": "piloto",
    "url": "git@des-gitlab.scae.redsara.es:JAVA/OTP-IC/Piloto-IC/piloto.git",
    "description": "",
    "homepage": "https://des-gitlab.scae.redsara.es/JAVA/OTP-IC/Piloto-IC/piloto"
  }
}

EOF

curl --insecure -H "Content-Type: application/json" --data @/tmp/example.json ${URL}
echo "RetVal: $?"
