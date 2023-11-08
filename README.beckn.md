## Certbot 
A domain agnostic, scriptable, headless, synchronous bap, that can be used by Bpp developers to self test their implementation. 

### How to use it?
Simply using curl, or postman you can "trigger" this bap to fire a call at your bpp's endpoint. 

e.g. 
curl -H 'X-CallBackToBeSynchronized: Y' -H 'content-type:application/json' -H 'Authorization Basic Y2VydGJvdC1odW1iaGlvbmxpbmUtaW46Y2VydGJvdA==' https://certbot-preprod.humbhionline.in/bap/api/bg/search -d '{"context":{"bpp_id":"preprod-wiggles.humbhionline.in","country":"IND","city":"std:080","domain":"ONDC:RET10","action":"search","core_version":"1.2.0","ttl":"PT30S","bap_uri":"https://certbot-preprod.humbhionline.in/bap","bap_id":"certbot-preprod.humbhionline.in"},"message":{"intent":{"payment":{"@ondc/org/buyer_app_finder_fee_type":"percent","@ondc/org/buyer_app_finder_fee_amount":"3"},"fulfillment":{"type":"Delivery"}}}}' 

You can pass transaction_id and message_id if you want. 
Notice that context has bpp_id  
This bap doesnot go via bg to the bpp, Simply does a lookup and send to the bpp url. 
So while testing your bpp, you may have to ignore the absence of X-GatewayAuthorization 


The bap waits for usual bpp's async response and sends that response  "as it as" synchronously to the triggering endpoint. 

###  Can I use in staging? 
*No*  it is currently only registered only in preprod for a variety of domains. If your domain is not supported, We can get it added. 

Id: 
Certbot-preprod.humbhiobline.in
Url 
https://certbot-preprod.humbhionline.in/bap
Trigger url 
https://certbot-preprod.humbhionline.in/bap/api/bg

### What about auth?
The trigger url is called with basic auth
The bap and Bpp communicates in the standard way 


### Test Flow Automation

Meet STREAM . (For String testing  rest api modules)

It is a flow test tool in which flows to be certified are declaratively defined and executed with assertions at each step. 

Sample flows for grocery domains are made availabe in the streams repository.  The test cases can be contributed by the community or created by ondc for purposes of certification. Streams is an open source project licensed under apache.2 license. 

### And the bap?
Currently free to use in preprod . We do plan to open source our bap sdk  in future . There is nothing special about this bap . As mentioned, It is a usual bap protocol server with a custom endpoint to trigger it. WE plan to a create a certification solution around these tools in future. Any ondc certified agency may be able use these tools and test cases to offer a signed certificate that certifies correct implementation of the bpp. We would also charge a small fee in the future for  certificates issued by  us. For  bpps who want to do their self test and accelate their testing, this service is free for now. If I run out of data limits, Will request Ondc/nsdl to host it. :) 
 

## Can this headless BAP be used to build experiential baps ?
* yes the tech is usable . we can provision a different bap id for any one building an experiential bap. 
