#!/usr/bin/env python3
"""
手工 zipalign：把 resources.arsc 存为不压缩，并让它的数据偏移按 4 字节对齐。
Android R+ 强制要求这一点，否则安装报 -124。

同时对所有 .so 做 4096 字节对齐（本模块没有 .so，保留逻辑以防万一）。
"""
import struct
import sys
import zipfile
import zlib

FORCE_STORE = {"resources.arsc"}
PAGE_ALIGN = {".so": 4096}


def dos_time(dt):
    t = (dt[3] << 11) | (dt[4] << 5) | (dt[5] // 2)
    d = ((dt[0] - 1980) << 9) | (dt[1] << 5) | dt[2]
    return t, d


def build_extra(gap):
    """生成一段合法的 zip extra field，长度为 gap 字节（gap 需 >= 4 或为 0）。"""
    if gap <= 0:
        return b""
    # 用 0xD935 作为 padding 标识（与 zipalign 一致）
    return struct.pack("<HH", 0xD935, gap - 4) + b"\x00" * (gap - 4)


def align(src, dst):
    zin = zipfile.ZipFile(src)
    entries = [(zi, zin.read(zi.filename)) for zi in zin.infolist()]

    out = open(dst, "wb")
    central = []

    for zi, data in entries:
        name = zi.filename
        nameb = name.encode("utf-8")

        force_store = name in FORCE_STORE
        method = zipfile.ZIP_STORED if force_store else zipfile.ZIP_DEFLATED

        if method == zipfile.ZIP_DEFLATED:
            co = zlib.compressobj(9, zlib.DEFLATED, -15)
            cdata = co.compress(data) + co.flush()
        else:
            cdata = data

        crc = zlib.crc32(data) & 0xFFFFFFFF
        csize = len(cdata)
        usize = len(data)
        dost, dosd = dos_time(zi.date_time)

        # 计算所需对齐
        align_to = 1
        for suffix, a in PAGE_ALIGN.items():
            if name.endswith(suffix):
                align_to = max(align_to, a)
        if force_store:
            align_to = max(align_to, 4)

        offset = out.tell()
        local_hdr_len = 30 + len(nameb)
        gap = 0
        if align_to > 1:
            gap = (-(offset + local_hdr_len)) % align_to
            if 0 < gap < 4:
                gap += align_to  # 保证 extra field 结构合法
        extra = build_extra(gap)

        # 本地文件头
        out.write(struct.pack(
            "<IHHHHHIIIHH",
            0x04034B50,      # 签名
            20,              # 解压所需版本
            0,               # flags
            method,          # 压缩方式
            dost, dosd,      # 时间/日期
            crc, csize, usize,
            len(nameb), len(extra),
        ))
        out.write(nameb)
        out.write(extra)
        data_offset = out.tell()
        out.write(cdata)

        if force_store:
            assert data_offset % 4 == 0, "resources.arsc 未对齐到 4 字节"

        central.append((
            nameb, method, dost, dosd, crc, csize, usize, offset, zi.external_attr
        ))

    cd_start = out.tell()
    for (nameb, method, dost, dosd, crc, csize, usize, offset, ext_attr) in central:
        out.write(struct.pack(
            "<IHHHHHHIIIHHHHHII",
            0x02014B50,      # 中央目录签名
            20,              # 创建版本
            20,              # 解压所需版本
            0,               # flags
            method,
            dost, dosd,
            crc, csize, usize,
            len(nameb),      # 文件名长度
            0,               # extra 长度（中央目录不写 padding）
            0,               # comment 长度
            0,               # 磁盘号
            0,               # 内部属性
            ext_attr,
            offset,
        ))
        out.write(nameb)

    cd_size = out.tell() - cd_start
    out.write(struct.pack(
        "<IHHHHIIH",
        0x06054B50,          # EOCD
        0, 0,                # 磁盘号
        len(central), len(central),
        cd_size, cd_start,
        0,                   # comment 长度
    ))
    out.close()


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("用法: align.py <输入.apk> <输出.apk>")
        sys.exit(1)
    align(sys.argv[1], sys.argv[2])
    print("对齐完成:", sys.argv[2])
