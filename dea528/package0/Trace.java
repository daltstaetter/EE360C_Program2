package package0;

public class Trace 
{
	public final int device;
	public final int time;
	private Integer devTx = null;
	private Integer devRx = null;
	private Integer devTime = null;
	
	/**
	 * Holds a touple of the device and communication time that
	 * it is in contact with another device
	 * @param dev device identification number
	 * @param comTime time at which dev communicates with another device
	 */
	public Trace(int dev, int comTime)
	{
		device = dev;
		time = comTime;
	}
	public Trace(int devTx0, int devRx0, int comTime0)
	{
		devTx = devTx0;
		devRx = devRx0;
		devTime = comTime0;
		device = -1;
		time = -1;
	}
	public Trace( int devTx0, Trace tr)
	{
		
		devTx = devTx0;
		devRx = tr.device;
		devTime = tr.time;
		device = -1;
		time = -1;
	}
	public int[] getPrivateVars()
	{
		int[] r = {devTx, devRx, devTime};
		return r;
	}
	
	
	public String toString()
	{
		String s;
		
		if(devTx == null)
		{
			s = "dev=" + device + " time="+ time;
		}
		else
		{
			s = "devTx=" + devTx + " devRx=" + devRx + " time="+ devTime;
		}
		return s;
	}
}
