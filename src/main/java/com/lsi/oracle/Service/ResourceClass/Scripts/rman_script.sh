#!/bin/bash
rman target / <<EOF
$1
EOF
