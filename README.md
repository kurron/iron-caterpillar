#Overview
The Iron Caterpillar project is a showcase of a microservice that can be deployed both on-premise and in the Amazon cloud.  
The architecture allows for replacing particular pieces of the solution based on the deployment context.  For example, in
the context of an on-premise deployment the caching solution utilizes Redis while in a cloud context the caching solution 
is memcached based.  We take advantage of Spring Environment facilities to use the correct strategies for a given context.   
