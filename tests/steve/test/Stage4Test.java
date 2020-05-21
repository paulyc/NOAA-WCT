package steve.test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.junit.Test;

import ucar.nc2.NCdumpW;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;


public class Stage4Test {
    
    @Test
    public void testOpendapAccess() throws IOException {
        
        String urlString = "http://nomads.ncdc.noaa.gov:8085/thredds/dodsC/radar/StIV/ST4_Agg_2005-TEST.nc";
        GridDataset gd = GridDataset.open(urlString);
        List<GridDatatype> gridList = gd.getGrids();
        for (GridDatatype grid : gridList) {
//            System.out.println(grid.getCoordinateSystem().getDateRange().);
//            grid.getCoordinateSystem().getTimeAxis().
        }
        
        
        StringWriter sw = new StringWriter();
        NCdumpW.print("http://eclipse.ncdc.noaa.gov:9090/thredds/fileServer/radar/StIV/ST4_Agg_2005-TEST.nc", sw, null);
        System.out.println(sw.toString());
    }

}
