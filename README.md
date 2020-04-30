# Jupyter OMERO/Fiji/Napari Desktop
[![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/ome/omero-analysis-desktop/master?filepath=napari.ipynb)


Run [OMERO clients](https://www.openmicroscopy.org/omero/downloads/), [Napari](http://napari.org/) and [omero-napari](https://gitlab.com/openmicroscopy/incubator/omero-napari) in a Linux desktop using Jupyter.

This is based on https://github.com/ryanlovett/nbnovnc

Either [run on mybinder.org](https://mybinder.org/v2/gh/ome/omero-analysis-desktop/master) or build locally with [repo2docker](https://repo2docker.readthedocs.io/):

To build locally:

 * Install [Docker](https://www.docker.com/) if required
 * Create a virtual environment and install repo2docker from PyPI.
 * Clone this repository
 * Run  ``repo2docker``. 
 * Depending on the permissions, you might have to run the command as an admin

```
pip install jupyter-repo2docker
git clone https://github.com/ome/omero-analysis-desktop.git
cd omero-analysis-desktop
repo2docker .
```

Open the displayed URL, then go to `/desktop` in the menu option on the right-hand side.
You should see a Linux desktop with icons for OMERO.insight and FIJI.

Use omero-napari
================

Once the desktop is open, open a terminal and activate the conda environment:

```
. /srv/conda/bin/activate notebook
```

In the same terminal you can then use the OMERO CLI client ``omero-napari``:

First connect to the server:

```omero login --server wss://idr.openmicroscopy.org/omero-ws --user public --password public```

Open an image:

```
omero napari view  --zarr --endpoint_url https://s3.embassy.ebi.ac.uk/ Image:6001240
```

Use napari
==========

Once the desktop is open go back to the main Jupyter Notebook window, open `napari.ipynb` and execute the cells one at a time. You should see Napari open in the desktop window.
