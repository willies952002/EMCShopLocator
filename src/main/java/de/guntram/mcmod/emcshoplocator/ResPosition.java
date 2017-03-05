package de.guntram.mcmod.emcshoplocator;

public class ResPosition {
    public String server;
    public int x1, x2, z1, z2;
    public int resNumber;
    
    static ResPosition nullResPosition = new ResPosition("no server", 0, 0, 0, 0, 0);
    
    ResPosition(String s, int x1, int x2, int z1, int z2, int n) {
        server=s;
        this.x1=x1;
        this.x2=x2;
        this.z1=z1;
        this.z2=z2;
        resNumber=n;
    }
    
    static ResPosition getResAt(String server, int x, int z) {
        ResPosition[] list=ResInit.positions.get(server.toLowerCase());
        if (list==null || list.length==0) {
            System.out.println("no positions for "+server);
            return nullResPosition;
        }
        System.out.println("search for x="+x+", z="+z+" on "+server);
        int min=0;
        int max=list.length-1;
        int mid=min;
        while (min<=max) {
            mid=(min+max)/2;
            System.out.println("   mid="+mid+", square is "+list[mid].x1+"-"+list[mid].x2+" , "+list[mid].z1+"-"+list[mid].z2);
            if (list[mid].x1 > x)
                max=mid-1;
            else if (list[mid].x2 < x)
                min=mid+1;
            else if (list[mid].z1 > z)
                max=mid-1;
            else if (list[mid].z2 < z)
                min=mid+1;
            else
                break;
        }
        System.out.println("end of loop: mid="+mid+", square is "+list[mid].x1+"-"+list[mid].x2+" , "+list[mid].z1+"-"+list[mid].z2);
        if (list[mid].x1 <= x && x <= list[mid].x2
        &&  list[mid].z1 <= z && z <= list[mid].z2)
            return list[mid];
        System.out.println("Didnt find res at "+x+"/"+z+" on server "+server);
        return nullResPosition;
    }
}
