use std::fs::create_dir_all;
use std::path::Path;
use std::mem;
use std::ffi::CStr;
use libc::uname;
use libbpf_cargo::SkeletonBuilder;


const BPF_SRCDIR: &str = "../../bpf";


fn main() {
    let kernelver = unsafe{
        let mut buf = mem::zeroed();
        uname(&mut buf);
        CStr::from_ptr(&buf.release as *const i8).to_str().unwrap()
    };
    let ipath = format!("/usr/src/kernels/{}", kernelver);

    create_dir_all(format!("{}/.output", BPF_SRCDIR)).unwrap();
    let bpfsrc = format!("{}/tty_read.bpf.c", BPF_SRCDIR);
    let skelsrc = format!("{}/.output/tty_read.skel.rs", BPF_SRCDIR);

    let skel = Path::new(&skelsrc);
    SkeletonBuilder::new()
        .source(&bpfsrc)
        .clang_args(format!("-I{}", ipath))
        .build_and_generate(&skel)
        .expect("bpf compilation failed");
    println!("cargo:rerun-if-changed={}", bpfsrc);
}
