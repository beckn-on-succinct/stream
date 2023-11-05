# STREAM - STring REst Api Modules

STREAM is used to string test rest apis.


## Writing String Tests
Each String testing Usecase  is defined in the form of a flow-name.json. 
Sample [here](./src/test/resources/ondc-1.2-ret10/flow1.json)
We use [nashorn](https://github.com/openjdk/nashorn)  library to resolve the variables in the testcase json

## Executing flows
```
java -cp "target/classes:target/dependency/*" in.humbhionline.certbot.TestRunner -g config.json -t test1.json -t test2.json ... 
or 
bin/certbot.sh -g config.json -t test1.json -t test2.json ... 
```

## Installing

1. Installing dependency
    * git clone git@github.com:Open-Succinct-Community/common.git 
    * cd common ;
    * mvn clean install 
2. Installing Stream
    * git clone git@github.com:beckn-on-succinct/certbot.git 
    * cd certbot ;
    * mvn clean compile;

## Creating a flow json file.
Basic structure of the flow file is :

```
{
    "name":"A name for the flow";

    "steps": [ 
        {
            "name": "step1", 
            "request": {
                "headers": {
                    ...
                },
                "body": {//The payload sent to the url associated to the request
                    "version": "${context.core_version}" //These $ variables are defined in config file passed via -g option while executing the flow
                },
                "method": "get|post|put|delete", // one of get,post...

                "timeout": 60, // Time out in seconds
                "url": "url to call"
            },
            "request_finalizer": "request.body.xyx = {}", 
            // You can additionally execute some js assignment statements  to manipulate the request. If you want to execute multiple statement you can use the alternate syntax with array. E.g. 
            "request_finalizer": [ "request.body.xyx = {};", 
                                   "request.body.abc = \"abc\";"  
                                   ],
            "assertion": {
                "script": {
                    "eval": "a js statement that evaluates to a boolean"
                }
            },
            "logs": [ //optional one of more log files. you can use to log request, response or any thing else you feel like like current state of variables,etc
                {
                    "name": "name-of-a-logfile",
                    "value": "A js expression that returns a string"
                },
            ]
        },
        {
            "name": "step2", 
            "request": {
                "headers": {
                    ...
                },
                "body": {// The body of the request being sent optional
                    "something" : "${step1.response.data.athing}",  // Set some thing from a thing in step1's response
                    ...

                },
                "method": "get|post|put|delete", // one of get,post...

                "timeout": 60, // Time out in seconds
                "url": "url to call"
            },
            "request_finalizer": "request.body.somethingelse = step1.request.body.version", 
            "assertion": {
                "script": {
                    "message" : "someotherthing in step2 response is someanotherthing in step1's response  " ,
                    "eval": "step2.response.data.someotherthing  === step1.response.data.someanotherthing && .." 
                    // you can check response.data or response.error or response.status
                }
            },
            "logs": [ //optional one of more log files. you can use to log request, response or any thing else you feel like like current state of variables,etc
                {
                    "name": "name-of-a-logfile",
                    "value": "A js expression that returns a string"
                },
            ]
        }
    ]

}
```


config file is a simple json file:

```
{
     "x"  : "A",
     "y"  : { "B" : "C" } 
}
It can be accessed as x or y.B in script regions and 
if you are using for substitions in requests like templates you can use "${x}" or "${y.B}"

```


## More on Assertions 
We support following assertions 

Type|Name|Attributes| Asserts
-|-|-|-
SimpleAssertion|script|message,eval| js statement pointed by eval attribute is true
CompoundAssertion (Array)|| none |  
| |or|  |if any of the assertion is true | 
| |and|  |if all of the assertions are true |




Most assertions can be done using just script. However convenience compound assertions can make your script code more manageable at times.
