#!/bin/sh

django_dir="$(dirname $(dirname $(realpath $0)) )"

find $django_dir -type f \( ! -iname "__init__.py" \) -path '*/authentication/migrations/*' -exec rm -rf {} \;
find $django_dir -type f \( ! -iname "__init__.py" \) -path '*/core/migrations/*' -exec rm -rf {} \;

find $django_dir -type d -name "__pycache__" -path '*/authentication/*' -exec rm -r {} \;
find $django_dir -type d -name "__pycache__" -path '*/core/*' -exec rm -r {} \;

[ -e $django_dir/db.sqlite3 ] && rm -rf $django_dir/db.sqlite3

echo "Done successfully."