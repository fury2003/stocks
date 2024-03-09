function openTabsWithUrls(urls) {
    urls.forEach(url => {
        window.open(url, '_blank');
    });
}


let urlList = [
    'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=SSI&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VND&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=HCM&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VCI&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=MBS&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=FTS&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VIX&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=SHS&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=BSI&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VCB&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=STB&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=CTS&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs'
];

urlList = [
    'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=CTG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=MBB&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=TCB&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=ACB&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VPB&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VIB&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=LPB&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=MSB&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=HDB&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=TPB&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=EIB&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=BID&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs'
];

urlList = [
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=PDR&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=DXG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=NVL&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VHM&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=NLG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=KDH&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=DIG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=CEO&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=HDC&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=NTL&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs'
];


urlList = [
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=CTD&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VCG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=FCN&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=HHV&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=CTR&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=THG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=C4G&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=DTD&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs'
]

urlList = [
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=KBC&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=IDC&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=SZC&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VGC&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=PHR&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=LHG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=DPR&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=NTC&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=GVR&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=BCM&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs'
]

urlList = [
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=MWG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=FRT&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=DGW&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=PET&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VNM&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VRE&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=PNJ&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs'	
]

urlList = [
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=DGC&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=CSV&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=LAS&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=DDV&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VIP&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=GMD&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=SGP&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=HAH&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VOS&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VSC&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VTP&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VHC&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=ANV&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=FMC&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=IDI&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=PC1&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=TV2&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=NT2&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=QTP&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=GEG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=HPG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=NKG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=HSG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs'
]

urlList = [
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=BSR&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=PVD&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=PVS&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=PVT&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=PLX&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=GAS&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=KSB&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=DHA&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VLB&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=HT1&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=BMP&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=NTP&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=DCM&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=DPM&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=PAN&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=LTG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=PTB&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VCS&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=DBC&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=HAG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=BAF&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=TNG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VGT&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs'
]

urlList = [
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=STK&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=MSH&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=ADS&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=GIL&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=QNS&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=SBT&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=LSS&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=SLS&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=DHG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=IMP&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=AMV&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=FPT&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=HAX&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VEA&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=HUT&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=DHC&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=DRC&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=REE&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=HDG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=GEX&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=MSN&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VIC&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VGS&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=PLC&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=VGI&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=SIP&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=BCG&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=EVF&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs',
	'https://iboard-api.ssi.com.vn/statistics/reports/export-le-table?lang=vi&stockSymbol=BWE&__cf_chl_tk=wgx7RSwj6IsuzVnsioRcNV7F43U7pfvH06TfI7CA5x4-1705655615-0-gaNycGzNELs'
]

openTabsWithUrls(urlList);