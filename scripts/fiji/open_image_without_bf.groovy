/*
 * -----------------------------------------------------------------------------
 *  Copyright (C) 2020 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * ------------------------------------------------------------------------------
 */

/*
 * This Groovy script shows how open an image without using Bio-Formats.
 * Use this script in the Scripting Dialog of Fiji (File > New > Script).
 * Select Groovy as language in the Scripting Dialog.
 * Error handling is omitted to ease the reading of the script but this
 * should be added if used in production to make sure the services are closed
 * Information can be found at
 * https://docs.openmicroscopy.org/latest/omero5/developers/Java.html
 */

#@ String(label="Username") USERNAME
#@ String(label="Password", style='password') PASSWORD
#@ String(label="Host", value='wss://workshop.openmicroscopy.org/omero-ws') HOST
#@ String(label="ImageID", value='55290') image_id

import java.util.ArrayList
import omero.gateway.Gateway
import omero.gateway.LoginCredentials
import omero.gateway.SecurityContext
import omero.gateway.facility.BrowseFacility
import omero.log.SimpleLogger


import loci.formats.FormatTools
import loci.formats.ImageTools
import loci.common.DataTools


import ij.IJ
import ij.ImagePlus
import ij.ImageStack
import ij.process.ByteProcessor
import ij.process.ShortProcessor
import ij.process.FloatPolygon


def connect_to_omero() {
    "Connect to OMERO"

    credentials = new LoginCredentials(USERNAME.trim(), PASSWORD.trim(), HOST)
    simpleLogger = new SimpleLogger()
    gateway = new Gateway(simpleLogger)
    gateway.connect(credentials)
    return gateway
}


def open_omero_image(ctx, image_id, value) {
	
    browse = gateway.getFacility(BrowseFacility)
    ids = new ArrayList(1)
    ids.add(new Long(image_id))
    images = browse.getImages(ctx, ids)
    image = images[0]
    pixels = image.getDefaultPixels()
    size_z = pixels.getSizeZ()
    size_t = pixels.getSizeT()
    size_c = pixels.getSizeC()
    size_x = pixels.getSizeX()
    size_y = pixels.getSizeY()
    pixtype = pixels.getPixelType()
    pixels_type = FormatTools.pixelTypeFromString(pixtype)
    bpp = FormatTools.getBytesPerPixel(pixels_type)
    is_signed = FormatTools.isSigned(pixels_type)
    is_float = FormatTools.isFloatingPoint(pixels_type)
    is_little = false
    interleave = false
    store = gateway.getPixelsStore(ctx)
    pixels_id = pixels.getId()
    store.setPixelsId(pixels_id, false)
    stack = new ImageStack(size_x, size_y)
    start = 0
    end = size_t
    dimension = size_t
    if (value >= 0 && value < size_t) {
        start = value
        end = value + 1
        dimension = 1
    } else if (value >= size_t) {
        throw new Exception("The selected timepoint cannot be greater than or equal to " + size_t)
    }
    t = start
    while (t < end) {
        z = 0
        while (z < size_z) {
            c = 0
            while (c < size_c) {
                plane = store.getPlane(z, c, t)

                ImageTools.splitChannels(plane, 0, 1, bpp, false, interleave)
                pixels = DataTools.makeDataArray(plane, bpp, is_float, is_little)

                q = pixels
                if (plane.size() != size_x*size_y) {
                    tmp = q
                    q = zeros(size_x*size_y, 'h')
                    System.arraycopy(tmp, 0, q, 0, Math.min(q.size(), tmp.size()))
                    if (is_signed) {
                        q = DataTools.makeSigned(q)
                    }
                }
                if (q[0] instanceof Byte) {
                    ip = new ByteProcessor(size_x, size_y, q, null)
                } else {
                    ip = new ShortProcessor(size_x, size_y, q, null)
                }
                stack.addSlice('', ip)
                c += 1
            }
            z += 1
        }
        t += 1
    }
    store.close()
    // Do something
    image_name = image.getName() + '--OMERO ID:' + image.getId()
    imp = new ImagePlus(image_name, stack)
    imp.setDimensions(size_c, size_z, dimension)
    imp.setOpenAsHyperStack(true)
    imp.show()
    return imp

}

//Connect to OMERO
gateway = connect_to_omero()
exp = gateway.getLoggedInUser()
group_id = exp.getGroupId()
group_id = exp.getGroupId()
ctx = new SecurityContext(group_id)

print "connected"
open_omero_image(ctx, image_id, 0)
IJ.run("8-bit")
// white might be required depending on the version of Fiji
IJ.run(imp, "Auto Threshold", "method=MaxEntropy stack")
IJ.run(imp, "Analyze Particles...", "size=10-Infinity pixel display clear add stack summarize")
IJ.run("Set Measurements...", "area mean standard modal min centroid center perimeter bounding feret's summarize stack display redirect=None decimal=3")

gateway.disconnect()
