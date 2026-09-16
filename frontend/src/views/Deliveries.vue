<template>
  <div>
    <el-card shadow="never">
      <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
        <el-select v-model="query.status" placeholder="按状态" clearable style="width:140px">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="query.batchId" placeholder="按批次" clearable style="width:200px">
          <el-option v-for="b in doneBatches" :key="b.id" :label="`${b.batchNo} · ${b.meal}`" :value="b.id" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button type="success" @click="openDelivery">开一张配送单</el-button>
        <span style="margin-left:auto;color:#909399">共 {{ rows.length }} 张</span>
      </div>
    </el-card>

    <el-table :data="rows" border stripe size="small" style="margin-top:12px" v-loading="loading">
      <el-table-column prop="deliveryNo" label="配送单号" width="120" />
      <el-table-column label="批次" width="190">
        <template #default="{ row }">{{ batchLabel(row.batchId) }}</template>
      </el-table-column>
      <el-table-column prop="customer" label="收货单位" min-width="150" />
      <el-table-column prop="portions" label="份数" width="90" />
      <el-table-column prop="driver" label="司机" width="100" />
      <el-table-column prop="deliverDate" label="发车日" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="tagType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button v-if="row.status === '待发'" link type="primary" @click="openGo(row)">发车</el-button>
          <template v-if="row.status === '在途'">
            <el-button link type="success" @click="advance(row, 'sign')">签收</el-button>
            <el-button link type="danger" @click="advance(row, 'back')">退回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" title="开一张配送单" width="500px">
      <el-form label-width="100px">
        <el-form-item label="批次">
          <el-select v-model="form.batchId" placeholder="只列已完工的批次" style="width:100%">
            <el-option
              v-for="b in doneBatches"
              :key="b.id"
              :label="`${b.batchNo} · ${dishName(b.dishId)} · 可发 ${remain(b)} 份`"
              :value="b.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="收货单位"><el-input v-model="form.customer" /></el-form-item>
        <el-form-item label="份数"><el-input-number v-model="form.portions" :min="1" :step="10" /></el-form-item>
        <el-form-item label="司机"><el-input v-model="form.driver" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">开单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="goVisible" title="发车" width="440px">
      <el-form label-width="100px">
        <el-form-item label="司机"><el-input v-model="goForm.driver" /></el-form-item>
        <el-form-item label="发车日期">
          <el-date-picker v-model="goForm.deliverDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="goVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGo">确定发车</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { batchApi, deliveryApi, dishApi } from '../api'

const statuses = ['待发', '在途', '已签收', '已退回']

const rows = ref([])
const batches = ref([])
const dishes = ref([])
const loading = ref(false)
const query = reactive({ status: '', batchId: null })

const visible = ref(false)
const form = reactive({ batchId: null, customer: '', portions: 100, driver: '' })
const goVisible = ref(false)
const goForm = reactive({ id: null, driver: '', deliverDate: '' })

const doneBatches = computed(() => batches.value.filter((b) => b.status === '已完成'))

const dishName = (id) => (id ? dishes.value.find((d) => d.id === id)?.name || `#${id}` : '—')
const batchLabel = (id) => {
  const b = batches.value.find((x) => x.id === id)
  return b ? `${b.batchNo} · ${dishName(b.dishId)}` : `#${id}`
}
const tagType = (s) =>
  s === '已签收' ? 'success' : s === '在途' ? 'warning' : s === '已退回' ? 'danger' : ''

const remain = (batch) => {
  const sent = rows.value
    .filter((r) => r.batchId === batch.id && r.status !== '已退回')
    .reduce((sum, r) => sum + r.portions, 0)
  return batch.actualPortions - sent
}

const load = async () => {
  loading.value = true
  try {
    rows.value = await deliveryApi.list({
      status: query.status || undefined,
      batchId: query.batchId || undefined
    })
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

const openDelivery = () => {
  Object.assign(form, { batchId: null, customer: '', portions: 100, driver: '' })
  visible.value = true
}

const submit = async () => {
  try {
    await deliveryApi.open({ ...form })
    ElMessage.success('配送单已开')
    visible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const openGo = (row) => {
  Object.assign(goForm, {
    id: row.id,
    driver: row.driver || '',
    deliverDate: new Date().toISOString().slice(0, 10)
  })
  goVisible.value = true
}

const submitGo = async () => {
  try {
    await deliveryApi.advance(goForm.id, 'send', goForm.driver, goForm.deliverDate)
    ElMessage.success('已发车')
    goVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const advance = async (row, action) => {
  try {
    await deliveryApi.advance(row.id, action, null, null)
    ElMessage.success('已更新')
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

onMounted(async () => {
  try {
    const [b, d] = await Promise.all([batchApi.list({}), dishApi.list({})])
    batches.value = b
    dishes.value = d
  } catch (e) {
    ElMessage.error(e.message)
  }
  await load()
})
</script>
