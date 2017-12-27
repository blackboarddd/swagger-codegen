'''
Created on Dec 27, 2017

@author: bizhu
'''
import os
import sys

def runcommand(input, output):
    str = "java -jar swagger-codegen-cli.jar generate -l java -i " + input + " -o " + output + " -Dapis -Dmodels --model-package com.microstrategy.yatiactions.rest.model --api-package com.microstrategy.yatiactions.rest.api "
    p = os.popen(str)
    print(p)
    
runcommand(sys.argv[1], sys.argv[2])
