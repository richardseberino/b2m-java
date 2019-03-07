#!/bin/bash
ab -k -c 100 -n 700000 http://localhost:9080/rsapp/checkout &
exit 0


