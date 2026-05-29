<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElCard, ElRow, ElCol, ElSkeleton } from 'element-plus'
import * as echarts from 'echarts'
import { analyticsApi, type AnalyticsTrend, type AnalyticsPage, type AnalyticsSource } from '@/api'

const trendChartRef = ref<HTMLDivElement>()
const topPagesChartRef = ref<HTMLDivElement>()
const sourcesChartRef = ref<HTMLDivElement>()

let trendChart: echarts.ECharts | null = null
let topPagesChart: echarts.ECharts | null = null
let sourcesChart: echarts.ECharts | null = null

const loading = ref(true)
const trendData = ref<AnalyticsTrend | null>(null)
const topPages = ref<AnalyticsPage[]>([])
const sources = ref<AnalyticsSource[]>([])

function initTrendChart() {
  if (!trendChartRef.value || !trendData.value) return

  if (trendChart) {
    trendChart.dispose()
  }

  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    title: {
      text: '7-Day Visitor Trend',
      left: 'center',
      textStyle: { fontSize: 16, fontWeight: 600 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['PV', 'UV'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendData.value.dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: 'PV',
        type: 'line',
        smooth: true,
        data: trendData.value.pv,
        areaStyle: { opacity: 0.3 },
        lineStyle: { width: 2 },
        itemStyle: { color: '#409eff' }
      },
      {
        name: 'UV',
        type: 'line',
        smooth: true,
        data: trendData.value.uv,
        areaStyle: { opacity: 0.3 },
        lineStyle: { width: 2 },
        itemStyle: { color: '#67c23a' }
      }
    ]
  })
}

function initTopPagesChart() {
  if (!topPagesChartRef.value || !topPages.value.length) return

  if (topPagesChart) {
    topPagesChart.dispose()
  }

  topPagesChart = echarts.init(topPagesChartRef.value)
  topPagesChart.setOption({
    title: {
      text: 'Top Pages',
      left: 'center',
      textStyle: { fontSize: 16, fontWeight: 600 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: topPages.value.map(p => p.title || p.path).slice(0, 10).reverse(),
      inverse: true
    },
    series: [
      {
        name: 'Views',
        type: 'bar',
        data: topPages.value.map(p => p.viewCount).slice(0, 10).reverse(),
        itemStyle: { color: '#409eff' },
        label: {
          show: true,
          position: 'right',
          formatter: '{c}'
        }
      }
    ]
  })
}

function initSourcesChart() {
  if (!sourcesChartRef.value || !sources.value.length) return

  if (sourcesChart) {
    sourcesChart.dispose()
  }

  sourcesChart = echarts.init(sourcesChartRef.value)
  sourcesChart.setOption({
    title: {
      text: 'Traffic Sources',
      left: 'center',
      textStyle: { fontSize: 16, fontWeight: 600 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: 0,
      type: 'scroll'
    },
    series: [
      {
        name: 'Sources',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: sources.value.map((s, i) => ({
          value: s.count,
          name: s.source,
          itemStyle: {
            color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399'][i % 5]
          }
        }))
      }
    ]
  })
}

function handleResize() {
  trendChart?.resize()
  topPagesChart?.resize()
  sourcesChart?.resize()
}

async function fetchData() {
  loading.value = true
  try {
    const [trendRes, pagesRes, sourcesRes] = await Promise.all([
      analyticsApi.getTrend(7),
      analyticsApi.getTopPages(10),
      analyticsApi.getSources()
    ])

    trendData.value = trendRes.data.data
    topPages.value = pagesRes.data.data
    sources.value = sourcesRes.data.data

    // Initialize charts after data is fetched
    setTimeout(() => {
      initTrendChart()
      initTopPagesChart()
      initSourcesChart()
    }, 100)
  } catch {
    // Handle error
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  topPagesChart?.dispose()
  sourcesChart?.dispose()
})
</script>

<template>
  <div class="analytics">
    <h1 class="page-title">Analytics</h1>

    <ElSkeleton v-if="loading" :rows="10" animated />

    <template v-else>
      <!-- Trend Chart -->
      <ElRow :gutter="20" class="chart-row">
        <ElCol :span="24">
          <ElCard>
            <div ref="trendChartRef" style="width: 100%; height: 400px"></div>
          </ElCard>
        </ElCol>
      </ElRow>

      <!-- Top Pages & Sources -->
      <ElRow :gutter="20" class="chart-row">
        <ElCol :xs="24" :lg="12">
          <ElCard>
            <div ref="topPagesChartRef" style="width: 100%; height: 400px"></div>
          </ElCard>
        </ElCol>
        <ElCol :xs="24" :lg="12">
          <ElCard>
            <div ref="sourcesChartRef" style="width: 100%; height: 400px"></div>
          </ElCard>
        </ElCol>
      </ElRow>
    </template>
  </div>
</template>

<style scoped>
.analytics {
  padding: 20px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0 0 24px;
}

.chart-row {
  margin-bottom: 20px;
}
</style>
