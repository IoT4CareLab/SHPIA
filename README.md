SHPIA: Smart Home Platform for Intelligent Applications
==================================================================

2020 - 2022, Florenc Demrozi <forenc.demorzi@uis.no> - specifications definition, design, and testing of the architecture. 

with contributions from:
- Marco Rafaello <marco.rafaello@studenti.univr.it> - Working on BackEnd and Frontend
- Christian Farina <christian.farina@studenti.univr.it> - Extending SHPIA to BLE broadcast communicaiton technology

How to run the server (first you need to install python and mongodb)
-------------

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
	 

How to create a user
-------------

In order to use the application you need to create a user.

	1 Open the environment, go to the directory where manage.py is located and run this command 
		python3 manage.py createsuperuser
		follow the steps to create the superuser

	2 After you created the user, run the server
	
	3 Open your browser and search
		ip address: port /admin/
		
		example: 192.168.1.10:8000/admin/

	4 Login with the user you just created to check if everything is ok.
	

How to run the applicaiton (first you need to install Android Studio)
-------------

Open the project SHPIA-APP with Android Studio (do not open shpia-main, ONLY SHPIA-APP). Now you need to modify some parameters:

	1 Open the class named NordicApiService in the folder SHPIA-APP\app\src\main\java\com\raffaello\nordic\model and replace the ip address in line 24 with your private ip that you used to run the server

	2 Open settings.xml in the folder SHPIA-APP\app\src\main\res\xml and do the same thing with the ip address in line 6 

	3 Run the application 

It is important to run the application on your smartphone, to do this you need to connect the smartphone with Android Studio. Do not use the emulator. To run the application you need to run the server first.


Supported Smartphones
-------------
Every smartphone with Android version > 5.2

Supported BLE Devices
-------------
Nordic Thingy 52 ---> https://www.nordicsemi.com/Products/Development-hardware/Nordic-Thingy-52


Acknowledgements
-------------
Thanks to IFIP International Internet of Things Conference reviewers for the preciouse suggestions related to SHPIA.

License
-------
Copyright 2022, Florenc Demrozi.
See the licensing GNU General Public License v3.0 documentation (https://choosealicense.com/licenses/gpl-3.0/).

Citing
-------------
When using SHPIA please cite the following publication:

Demrozi, F., Pravadelli, G. (2022). _SHPIA: A Low-Cost Multi-purpose Smart Home Platform for Intelligent Applications_. In: Camarinha-Matos, L.M., Ribeiro, L., Strous, L. (eds) Internet of Things. IoT through a Multi-disciplinary Perspective. IFIPIoT 2022. IFIP Advances in Information and Communication Technology, vol 665. Springer, Cham. https://doi.org/10.1007/978-3-031-18872-5_13
