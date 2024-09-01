package com.yasuenag.garakuta.randbench;

import java.security.Provider;
import java.util.Map;

import com.yasuenag.garakuta.randbench.spi.RdRandSpi;
import com.yasuenag.garakuta.randbench.spi.RdSeedSpi;
import com.yasuenag.garakuta.randbench.spi.GetRandomBlockedSpi;
import com.yasuenag.garakuta.randbench.spi.GetRandomNonBlockedSpi;


public class RandBenchProvider extends Provider{

  public RandBenchProvider(){
    super("RandBenchProvider", "0.1.0", "RNG for benchmark: RDRAND/RDSEED/getrandom");

    var attrs = Map.of("ThreadSafe", "true",
                       "ImplementedIn", "Hardware");
    putService(new Provider.Service(this, "SecureRandom", "RdRand", RdRandSpi.class.getName(), null, attrs));
    putService(new Provider.Service(this, "SecureRandom", "RdSeed", RdSeedSpi.class.getName(), null, attrs));
    putService(new Provider.Service(this, "SecureRandom", "GetRandomBlocked", GetRandomBlockedSpi.class.getName(), null, attrs));
    putService(new Provider.Service(this, "SecureRandom", "GetRandomNonBlocked", GetRandomNonBlockedSpi.class.getName(), null, attrs));
  }

}
