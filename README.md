# SHPIA: Smart Home Platform for Intelligent Applications
==================================================================

2020 - 2022, Florenc Demrozi <forenc.demorzi@uis.no> - specifications definition, design, and testing of the architecture. 

with contributions from:
- Marco Rafaello <marco.rafaello@studenti.univr.it> - Working on BackEnd and Frontend
- Christian Farina <christian.farina@studenti.univr.it> - Extending SHPIA to BLE broadcast communicaiton technology

## HOW TO RUN THE SERVER (first you need to install python and mongodb)
-------------

Tutorial for mongodb installation: https://www.youtube.com/watch?v=oC6sKlhz0OE

Even though it is not necessary, it is a good practice to set a virtual environment with python. 

	1) Create a virtual environment called env1 in a directory
		python3 -m venv env1

	2) Activate the environment
		env1\Scripts\activate

	3) Go to the directory SHPIA-API\nordic-api-master (you will find a requirements.txt with the dependencies) and run this command:
		pip3 install -r requirements.txt

	4) Go to nordic-api-master\src and run the server
		python3 manage.py runserver <your private ip: port>
		
		EXAMPLE: python3 manage.py runserver 192.168.1.10:8000
		(to know your ip address, open the cmd and run ipconfig)
		
If you have some errors try to execute the file named wipe.sh in SHPIA-API\nordic-api-master\src\bin, then run the following commands:
	
	1) python manage.py makemigrations

	2) python manage.py migrate

	3) run the server again
	 

## HOW TO CREATE A USER

In order to use the application you need to create a user.

	1) Open the environment, go to the directory where manage.py is located and run this command 
		python3 manage.py createsuperuser
		follow the steps to create the superuser

	2) After you created the user, run the server
	
	3) Open your browser and search
		ip address:port/admin/
		
		EXAMPLE: 192.168.1.10:8000/admin/

	4) Login with the user you just created to check if everything is ok.
	

## HOW TO RUN THE APPLICATION (first you need to install Android Studio)

Open the project SHPIA-APP with Android Studio (do not open shpia-main, ONLY SHPIA-APP). Now you need to modify some parameters:

	1) Open the class named NordicApiService in the folder SHPIA-APP\app\src\main\java\com\raffaello\nordic\model and replace the ip address in line 24 with your private ip that you used to run the server

	2) Open settings.xml in the folder SHPIA-APP\app\src\main\res\xml and do the same thing with the ip address in line 6 

	3) Run the application 

It is important to run the application on your smartphone, to do this you need to connect the smartphone with Android Studio. Do not use the emulator. To run the application you need to run the server first.

steps to connect your device to AndroidStudio (follow step 3 and 4 only): https://developer.android.com/codelabs/basic-android-kotlin-compose-connect-device#2


## HOW TO SET SENSORS
Nordics: ther is no need to set parameters

Beacon Tags: in order to use tags you need to turn on the temperature using  the Beacony App and to activate G-Sensor-Mode as well. The data collector does not interpret accurately the bytes if you do not turn on these two parameters. 

## HOW TO SEE STORED DATA 
I suggest to install MongoDBCompass: https://compass.mongodb.com/api/v2/download/latest/compass/stable/windows


After you launched the application, try to create an environment and to add a few sensors, then start data collection. The device will start collecting data and the server on your pc (you have to start the server on your pc before starting the application, the android device must be connected to the same network) will save the environment values you have decided to collect. To decide wich values you want to collect, go to settings and select the kinds of data. After you stopped data collection, open MongoDBCompass, connect it to mongodb and check if the environment data has been saved on the database called nordic. Before you start scanning or collecting data, you need to turn on bluetooth and geolocation.


License
-------
Copyright 2022, Florenc Demrozi.
See the licensing GNU General Public License v3.0 documentation (https://choosealicense.com/licenses/gpl-3.0/).

Citing
-------------
When using SHPIA please cite the following publication:

Demrozi, F., Pravadelli, G. (2022). _SHPIA: A Low-Cost Multi-purpose Smart Home Platform for Intelligent Applications_. In: Camarinha-Matos, L.M., Ribeiro, L., Strous, L. (eds) Internet of Things. IoT through a Multi-disciplinary Perspective. IFIPIoT 2022. IFIP Advances in Information and Communication Technology, vol 665. Springer, Cham. https://doi.org/10.1007/978-3-031-18872-5_13

