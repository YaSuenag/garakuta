use std::ffi::CString;
use std::io::{stdin, stdout, Error, Write};
use std::mem;

use clap::Parser;
use libc::stat;

#[path = "../../../bpf/.output/tty_read.skel.rs"]
mod imp;
use imp::TtyReadSkelBuilder;


#[derive(Parser, Debug)]
struct Args {
    #[arg(value_name = "TTY device file")]
    pos_arg: String,
}

fn main() -> Result<(), Error> {
    let args = Args::parse();
    let devname = args.pos_arg;

    let skel_builder = TtyReadSkelBuilder::default();
    let open_skel = skel_builder.open().unwrap();
    let mut skel = open_skel.load().unwrap();

    unsafe {
        let c_str = CString::new(devname.clone()).unwrap();
        let mut statst = mem::zeroed();
        stat(c_str.as_ptr(), &mut statst);

        skel.data().tty_ino = statst.st_ino as i32;
    }

    skel.attach().unwrap();

    println!("Snooping {} (ino={})", devname, skel.data().tty_ino);
    println!("Press Enter to exit...");
    stdout().flush().unwrap();
    let mut line = String::new();
    let _ = stdin().read_line(&mut line);

    Ok(())
}
