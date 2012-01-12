/*
 * Copyright 2012  Matthew Mole <code@gairne.co.uk>, Carlos Eduardo da Silva <kaduardo@gmail.com>
 * 
 * This file is part of ahgdc.
 * 
 * ahgdc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ahgdc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ahgdc.  If not, see <http://www.gnu.org/licenses/>.
 */

package android.hgd;

public class PeriodicThread extends Thread {

	private long period;
	private WorkerThread worker;
	
	public PeriodicThread(long period, WorkerThread worker) {
		this.period = period;
		this.worker = worker;
	}
	
	public synchronized void setPeriod(long newPeriod) {
		this.period = newPeriod;
	}
	
	@Override
	public void run() {
		while (true) {
			loop();
		}
	}
	
	public void loop() {
		try {
			while(true) {
				if (period > 0) {
					Thread.sleep(period);
					worker.getPlaylist();
				}
				else {
					Thread.sleep(30000);
				}
			}
		} catch (InterruptedException e) {
			
		}
		
	}
}
