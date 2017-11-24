package com.pf.dedup;

public class ZZZOld {

	/*
	private void testGeneratedHash() throws Exception {
		ArrayList<String[]> data = MM.fileReadToLin_eelements(
				"/tmp/dedup/hash.txt", "#", "\\|", null);
		for(int i=0; i < data.size(); i++) {
			String[] line = data.get(i);
			
			System.out.println("\nNow doing file: " + line[2]);			
			File f = new File(line[2]);
			System.out.println("   Existed: " + String.valueOf(f.exists()));
			byte[] dataBA = new byte[1024*1024*10];
			FileInputStream fin = new FileInputStream(line[2]);
			int readBytes = fin.read(dataBA);
			fin.close();
			
			String hash = calculateHash(f);
			System.out.println("   Read bytes: " + readBytes);
			System.out.println("   Name: " + f.getPath() + ", match: " + String.valueOf(line[2].equals(f.getPath())));
			System.out.println("   Size: " + f.length() + ", match: " + String.valueOf(line[1].equals(String.valueOf(f.length()))));
			System.out.println("   Hash: " + hash + ", match: " + String.valueOf(line[0].equals(hash)));
		}
	}
	*/
	
	// Testing
	/*
	FileOutputStream fout = new FileOutputStream("hello.txt");
	fout.write(("Hello how are you: " + new java.util.Date().toString() + "\n").getBytes());
	fout.close();
	FileInputStream fin = new FileInputStream("hello.txt");
	byte[] data = new byte[100];
	int length = fin.read(data);
	System.out.println(new String(data, 0, length) + "\n");
	fin.close();
	*/
}
