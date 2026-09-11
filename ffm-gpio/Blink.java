import java.lang.foreign.*;


public class Blink{

  private static final long PERIPHERAL_BASE_ADDRESS = 0xFE000000L; // RPi 4
  private static final long GPIO_REGISTER_OFFSET    = 0x00200000L;

  private static final int GPFSEL0_ofs = 0;
  private static final int GPSET0_ofs  = 0x1C;
  private static final int GPCLR0_ofs  = 0x28;

  private void setOut(MemorySegment mmio, int gpio){
    int GPFSEL0 = mmio.get(ValueLayout.JAVA_INT, GPFSEL0_ofs);
    GPFSEL0 &= ~(0b111 << (gpio * 3));
    GPFSEL0 |= 1 << (gpio * 3);
    mmio.set(ValueLayout.JAVA_INT, GPFSEL0_ofs, GPFSEL0);
  }

  private void high(MemorySegment mmio, int gpio){
    mmio.set(ValueLayout.JAVA_INT, GPSET0_ofs, 1 << gpio);
  }

  private void low(MemorySegment mmio, int gpio){
    mmio.set(ValueLayout.JAVA_INT, GPCLR0_ofs, 1 << gpio);
  }

  public void main(){
    int devFD = -1;
    MemorySegment mmio = MemorySegment.NULL;

    try{
      devFD = Syscall.open("/dev/mem", Syscall.O_RDWR | Syscall.O_SYNC);
      mmio = Syscall.mmap(
                 MemorySegment.NULL,
                 4096L, // 1 page
                 Syscall.PROT_READ | Syscall.PROT_WRITE,
                 Syscall.MAP_SHARED,
                 devFD,
                 PERIPHERAL_BASE_ADDRESS + GPIO_REGISTER_OFFSET
             );

      final int GPIO = 5;
      setOut(mmio, GPIO);

      for(int cnt = 0; cnt < 3; cnt++){
        high(mmio, GPIO);
        Thread.sleep(500);
        low(mmio, GPIO);
        Thread.sleep(500);
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
    finally{
      if(mmio != MemorySegment.NULL){
        Syscall.munmap(mmio, 4096L);
      }
      if(devFD != -1){
        Syscall.close(devFD);
      }
    }
  }

}
