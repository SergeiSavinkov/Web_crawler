#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"

mvn clean compile dependency:copy-dependencies

mpjrun.sh -np 4 \
  -cp "target/classes:target/dependency/jsoup-1.18.2.jar:lib/mpj.jar" \
  crawler.Main \
  "https://famnit.upr.si"