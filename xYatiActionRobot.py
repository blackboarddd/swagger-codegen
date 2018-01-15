'''
Created on Dec 27, 2017

@author: bizhu
p = os.popen(str)
'''
import os
import sys
import subprocess

def runcommand():
	print("*** Tool will generate xYati Actions for REST API.             ***")
	print("*** Problem please contact with Zhu, Bingjie or Zhu, Lingping. ***")
	print("*** Attention: please make sure before execution:              ***")
	print("***  1. Dictionary Java.ydic is under Z:\Yati\Dictionaries;    ***")
	print("***  2. YAML spec and swagger-codegen-cli.jar is in this folder***")
	print("*** Please follow the Guidance to finish the generation.       ***")
	YAMLFileName = raw_input("YAML File Name:")
	if not os.path.isfile(YAMLFileName):
		print("File \"" + YAMLFileName + "\" not in this folder. Please input correct YAML File.")
		return
	OutPutFolderName = raw_input("Output Folder Name:")
	str = "java -jar swagger-codegen-cli.jar generate -l java -i " + YAMLFileName + " -o " + OutPutFolderName + " -Dapis -Dmodels --model-package com.microstrategy.yatiactions.rest.model --api-package com.microstrategy.yatiactions.rest.api "
	print(str)
	os.system(str)
	print("\n!!!Generation is Finished! Files are placed in folder \"" + OutPutFolderName +"\".")
    
runcommand()
