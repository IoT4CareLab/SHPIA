# SHPIA: Smart Home Platform for Intelligent Applications
HOW TO RUN THE SERVER (first you need to install python and mongodb)

Even though it is not necessary, it is a good practice to set a virtual environment with python. 

	1 Create a virtual environment called env1 in a directory
		python3 -m venv env1

	2 Activate the environment
		env1\Scripts\activate

	3 Go to the directory SHPIA-API\nordic-api-master (you will find a requirements.txt with the dependencies) and run this command:
		pip3 install -r requirements.txt

	4 Go to nordic-api-master\src and run the server
		python3 manage.py runserver <your private ip: port>
		
		example: python3 manage.py runserver 192.168.1.10:8000
		(to know your ip address, open the cmd and run ipconfig)
		
If you have some errors try to execute the file named wipe.sh in SHPIA-API\nordic-api-master\src\bin, then run the following commands:
	
	1 python manage.py makemigrations

	2 python manage.py migrate

	3 run the server again
	 

HOW TO CREATE A USER

In order to use the application you need to create a user.

	1 Open the environment, go to the directory where manage.py is located and run this command 
		python3 manage.py createsuperuser
		follow the steps to create the superuser

	2 After you created the user, run the server
	
	3 Open your browser and search
		ip address: port /admin/
		
		example: 192.168.1.10:8000/admin/

	4 Login with the user you just created to check if everything is ok.
	

HOW TO RUN THE APPLICATION (first you need to install Android Studio)

Open the project SHPIA-APP with Android Studio (do not open shpia-main, ONLY SHPIA-APP). Now you need to modify some parameters:

	1 Open the class named NordicApiService in the folder SHPIA-APP\app\src\main\java\com\raffaello\nordic\model and replace the ip address in line 24 with your private ip that you used to run the server

	2 Open settings.xml in the folder SHPIA-APP\app\src\main\res\xml and do the same thing with the ip address in line 6 

	3 Run the application 

It is important to run the application on your smartphone, to do this you need to connect the smartphone with Android Studio. Do not use the emulator. To run the application you need to run the server first.


SUPPORTED SMARTPHONES
Every smartphone with Android version > 5.2

SUPPORTED BLE Devices 
Nordic Thingy 52 ---> https://www.nordicsemi.com/Products/Development-hardware/Nordic-Thingy-52

