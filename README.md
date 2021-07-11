# tellmi
cmup project

In order to run the whole setup, the following steps must be taken:
1) run myapp.py:  python3 myapp.py
2) curl the objects to the server indicating sensor(id), video(name of the file), language(en, pt, fr, etc), age (young/adult), for example:  
	curl -d "sensor=1&video=monalisa-en-adult.mp3&language=en&age=adult" localhost:5000/video 
3) run the pycom code assigning each pycom an id in the url (it's commented), in this instance for example, sensor with id 1 would correspond to the mona lisa painting
4) insert the ip of running server on MainActivity.kt (it's commented there)
5) with everything running, one must only run the app and login with one of the following profiles:
	username:englishadult@gmail.com	password:password123
	
	username:frenchadult@gmail.com	password:password123
	
	username:adultopt@gmail.com		password:password123
	
	username:criancapt@gmail.com		password:password123
	

